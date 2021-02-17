package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.ChangeRecord
import fi.vm.dpm.diff.model.ChangeRecordComparator
import fi.vm.dpm.diff.model.ChangeReport
import fi.vm.dpm.diff.model.ChangeReportKind
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.ReportGeneratorDescriptor
import fi.vm.dpm.diff.model.ReportSection
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.model.SourceRecord
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.model.diagnostic.ValidationResults
import fi.vm.dpm.diff.model.metrics.TimeMetrics
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class StepKind {
    LOAD_BASELINE,
    LOAD_CURRENT,
    RESOLVE_CHANGES,
    SORT_CHANGES
}

private data class PartitionChangeRecords(
    val changes: List<ChangeRecord>,
    val baselineSourceRecordCount: Int,
    val currentSourceRecordCount: Int
)

private typealias StepAction = () -> Any
private typealias StepDiagnosticHandler = (step: StepKind, stepAction: StepAction) -> Any

class SqlReportGenerator(
    private val reportKind: ChangeReportKind,
    private val sectionPlans: Collection<SectionPlanSql>,
    private val sourceDbs: SourceDbs,
    private val reportGeneratorDescriptor: ReportGeneratorDescriptor,
    private val reportGenerationOptions: List<String>,
    private val diagnostic: Diagnostic
) {

    fun generateReport(): ChangeReport {

        val reportSections = sectionPlans.map {
            generateReportSection(it)
        }

        return ChangeReport(
            reportKind = reportKind,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")),
            baselineFileName = sourceDbs.baselineDbPath.fileName.toString(),
            currentFileName = sourceDbs.currentDbPath.fileName.toString(),
            sections = reportSections,
            reportGeneratorDescriptor = reportGeneratorDescriptor,
            reportGenerationOptions = reportGenerationOptions
        )
    }

    private fun generateReportSection(sectionPlan: SectionPlanSql): ReportSection {
        diagnostic.info("\n\nSection: ${sectionPlan.sectionOutline.sectionShortTitle}")

        sectionPlan.validate(diagnostic)

        val sectionGenerationMetrics = TimeMetrics(
            StepKind.LOAD_BASELINE to "Loading baseline records",
            StepKind.LOAD_CURRENT to "Loading current records",
            StepKind.RESOLVE_CHANGES to "Resolving changes",
            StepKind.SORT_CHANGES to "Sorting changes"
        )

        val progressIndication = diagnostic.progressIndication()

        val reportSection = doGenerateReportSection(sectionPlan) { stepKind: StepKind, stepAction: StepAction ->

            progressIndication.handleStep()
            sectionGenerationMetrics.startStep(stepKind)

            val result = stepAction()

            sectionGenerationMetrics.stopStep(stepKind)

            result
        }

        progressIndication.handleDone()

        diagnostic.verbose("Section generation metrics:")
        diagnostic.verbose(sectionGenerationMetrics.report())

        diagnostic.info("Baseline records: ${reportSection.baselineSourceRecords}")
        diagnostic.info("Current records: ${reportSection.currentSourceRecords}")
        diagnostic.info("Total changes: ${reportSection.changes.size}")

        return reportSection
    }

    private fun doGenerateReportSection(
        sectionPlan: SectionPlanSql,
        stepDiagnosticHandler: StepDiagnosticHandler
    ): ReportSection {

        val allPartitionChangeRecords = findSectionChangesForAllPartitions(
            sectionPlan,
            stepDiagnosticHandler
        )

        validatePartitionChangeRecordCounts(
            allPartitionChangeRecords,
            sectionPlan
        )

        val changes = combinePartitionedSectionChanges(
            allPartitionChangeRecords,
            sectionPlan,
            stepDiagnosticHandler
        )

        return ReportSection(
            sectionOutline = sectionPlan.sectionOutline,
            baselineSourceRecords = allPartitionChangeRecords.sumBy { it.baselineSourceRecordCount },
            currentSourceRecords = allPartitionChangeRecords.sumBy { it.currentSourceRecordCount },
            changes = changes
        )
    }

    private fun findSectionChangesForAllPartitions(
        sectionPlan: SectionPlanSql,
        stepDiagnosticHandler: StepDiagnosticHandler
    ): List<PartitionChangeRecords> {

        return sectionPlan.partitionedQueries.mapIndexed { partitionIndex, partitionQuery ->

            findSectionChangesForPartition(
                partitionQuery,
                partitionIndex,
                sectionPlan.partitionedQueries.size,
                sectionPlan,
                stepDiagnosticHandler
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun findSectionChangesForPartition(
        partitionQuery: String,
        partitionIndex: Int,
        totalPartitions: Int,
        sectionPlan: SectionPlanSql,
        stepDiagnosticHandler: StepDiagnosticHandler
    ): PartitionChangeRecords {
        val baselineSourceRecords = stepDiagnosticHandler(StepKind.LOAD_BASELINE) {
            loadSourceRecordsPartition(
                partitionQuery = partitionQuery,
                partitionIndex = partitionIndex,
                totalPartitions = totalPartitions,
                queryColumnMapping = sectionPlan.queryColumnMapping,
                sectionOutline = sectionPlan.sectionOutline,
                dbConnection = sourceDbs.baselineConnection
            )
        } as List<SourceRecord>

        val currentSourceRecords = stepDiagnosticHandler(StepKind.LOAD_CURRENT) {
            loadSourceRecordsPartition(
                partitionQuery = partitionQuery,
                partitionIndex = partitionIndex,
                totalPartitions = totalPartitions,
                queryColumnMapping = sectionPlan.queryColumnMapping,
                sectionOutline = sectionPlan.sectionOutline,
                dbConnection = sourceDbs.currentConnection
            )
        } as List<SourceRecord>

        val changes = stepDiagnosticHandler(StepKind.RESOLVE_CHANGES) {
            ChangeRecord.resolveChanges(
                sectionOutline = sectionPlan.sectionOutline,
                baselineSourceRecords = baselineSourceRecords,
                currentSourceRecords = currentSourceRecords
            )
        } as List<ChangeRecord>

        return PartitionChangeRecords(
            changes = changes,
            baselineSourceRecordCount = baselineSourceRecords.size,
            currentSourceRecordCount = currentSourceRecords.size
        )
    }

    private fun loadSourceRecordsPartition(
        partitionQuery: String,
        partitionIndex: Int,
        totalPartitions: Int,
        queryColumnMapping: Map<String, Field>,
        sectionOutline: SectionOutline,
        dbConnection: DbConnection
    ): List<SourceRecord> {

        return dbConnection.executeQuery(
            query = partitionQuery,
            queryDebugName = "${sectionOutline.sectionTitle} SourceRecordsPartition ${partitionIndex + 1}/$totalPartitions"
        ) { resultSet ->

            QueryColumnMappingValidator.validateColumnNamesMatch(
                queryColumnMapping,
                resultSet.metaData,
                diagnostic
            )

            val sourceRecords = mutableListOf<SourceRecord>()

            while (resultSet.next()) {

                val sourceRecord = SourceRecord(
                    fields = resultSet.mapColumnValuesToFields(queryColumnMapping),
                    sectionOutline = sectionOutline,
                    sourceKind = dbConnection.sourceKind
                )

                sourceRecords.add(sourceRecord)
            }

            sourceRecords
        }
    }

    private fun ResultSet.mapColumnValuesToFields(
        queryColumnMapping: Map<String, Field>
    ): Map<Field, String> {
        return queryColumnMapping.map { (columnLabel, field) ->
            field to getString(columnLabel)
        }.toMap()
    }

    private fun validatePartitionChangeRecordCounts(
        partitionChangeRecords: List<PartitionChangeRecords>,
        sectionPlan: SectionPlanSql
    ) {
        SourceRecordCountValidator.validateCountWithSourceTableTotalRows(
            partitionChangeRecords.sumBy { it.baselineSourceRecordCount },
            sectionPlan.sourceTableDescriptors,
            sourceDbs.baselineConnection,
            sectionPlan.sectionOutline.sectionTitle,
            diagnostic
        )

        SourceRecordCountValidator.validateCountWithSourceTableTotalRows(
            partitionChangeRecords.sumBy { it.currentSourceRecordCount },
            sectionPlan.sourceTableDescriptors,
            sourceDbs.currentConnection,
            sectionPlan.sectionOutline.sectionTitle,
            diagnostic
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun combinePartitionedSectionChanges(
        partitionChangeRecords: List<PartitionChangeRecords>,
        sectionPlan: SectionPlanSql,
        stepDiagnosticHandler: StepDiagnosticHandler
    ): List<ChangeRecord> {

        return stepDiagnosticHandler(StepKind.SORT_CHANGES) {
            partitionChangeRecords
                .flatMap { it.changes }
                .sortedWith(ChangeRecordComparator(sectionPlan.sectionOutline.sectionSortOrder))
        } as List<ChangeRecord>
    }

    private fun SectionPlanSql.validate(diagnostic: Diagnostic) {
        val validationResults = ValidationResults()

        sectionOutline.validate(validationResults)
        validate(validationResults)

        validationResults.reportErrors(diagnostic)
    }
}
