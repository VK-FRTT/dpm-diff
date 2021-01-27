package fi.vm.dpm.diff.model

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.model.metrics.TimeMetrics
import fi.vm.dpm.diff.repgen.DbConnection
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.SourceDbs
import fi.vm.dpm.diff.repgen.dpm.utils.SourceTableDescriptor
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
        diagnostic.info("\n\nSection: ${sectionPlan.sectionOutline().sectionTitle}")

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

        sectionPlan.sanityCheck()

        val allPartitionChangeRecords = findSectionChangesForAllPartitions(
            sectionPlan,
            stepDiagnosticHandler
        )

        sanityCheckPartitionChangeRecords(
            allPartitionChangeRecords,
            sectionPlan
        )

        val changes = combinePartitionedSectionChanges(
            allPartitionChangeRecords,
            sectionPlan,
            stepDiagnosticHandler
        )

        return ReportSection(
            sectionOutline = sectionPlan.sectionOutline(),
            baselineSourceRecords = allPartitionChangeRecords.sumBy { it.baselineSourceRecordCount },
            currentSourceRecords = allPartitionChangeRecords.sumBy { it.currentSourceRecordCount },
            changes = changes
        )
    }

    private fun findSectionChangesForAllPartitions(
        sectionPlan: SectionPlanSql,
        stepDiagnosticHandler: StepDiagnosticHandler
    ): List<PartitionChangeRecords> {

        val partitionedQueries = sectionPlan.partitionedQueries()
        val totalPartitions = partitionedQueries.size

        return partitionedQueries.mapIndexed { partitionIndex, partitionQuery ->

            findSectionChangesForPartition(
                partitionQuery,
                partitionIndex,
                totalPartitions,
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
                queryColumnMapping = sectionPlan.queryColumnMapping(),
                sectionOutline = sectionPlan.sectionOutline(),
                dbConnection = sourceDbs.baselineConnection
            )
        } as List<SourceRecord>

        val currentSourceRecords = stepDiagnosticHandler(StepKind.LOAD_CURRENT) {
            loadSourceRecordsPartition(
                partitionQuery = partitionQuery,
                partitionIndex = partitionIndex,
                totalPartitions = totalPartitions,
                queryColumnMapping = sectionPlan.queryColumnMapping(),
                sectionOutline = sectionPlan.sectionOutline(),
                dbConnection = sourceDbs.currentConnection
            )
        } as List<SourceRecord>

        val changes = stepDiagnosticHandler(StepKind.RESOLVE_CHANGES) {
            ChangeRecord.resolveChanges(
                sectionOutline = sectionPlan.sectionOutline(),
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

            sanityCheckResultSetColumnLabels(
                queryColumnMapping,
                resultSet
            )

            val sourceRecords = mutableListOf<SourceRecord>()

            while (resultSet.next()) {

                val sourceRecord = SourceRecord(
                    sectionOutline = sectionOutline,
                    sourceKind = dbConnection.sourceKind,
                    fields = resultSet.mapColumnValuesToFields(queryColumnMapping)
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

    private fun sanityCheckPartitionChangeRecords(
        partitionChangeRecords: List<PartitionChangeRecords>,
        sectionPlan: SectionPlanSql
    ) {
        sanityCheckLoadedSourceRecordCount(
            partitionChangeRecords.sumBy { it.baselineSourceRecordCount },
            sectionPlan.sourceTableDescriptors(),
            sourceDbs.baselineConnection,
            sectionPlan.sectionOutline()
        )

        sanityCheckLoadedSourceRecordCount(
            partitionChangeRecords.sumBy { it.currentSourceRecordCount },
            sectionPlan.sourceTableDescriptors(),
            sourceDbs.currentConnection,
            sectionPlan.sectionOutline()
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
                .sortedWith(ChangeRecordComparator(sectionPlan.sectionOutline().sectionSortOrder))
        } as List<ChangeRecord>
    }

    private fun sanityCheckResultSetColumnLabels(
        queryColumnMapping: Map<String, Field>,
        resultSet: ResultSet
    ) {
        val resultSetColumnLabels =
            (1..resultSet.metaData.columnCount)
                .map { resultSet.metaData.getColumnLabel(it) }
                .toTypedArray()

        val mappingColumnLabels =
            queryColumnMapping
                .map { (columnLabel, _) -> columnLabel }
                .toTypedArray()

        if (!(resultSetColumnLabels contentDeepEquals mappingColumnLabels)) {
            diagnostic.fatal(
                """
                ResultSet and ColumnMapping mismatch.
                ResultSet columns: ${resultSetColumnLabels.toList()}
                ColumnMapping columns: ${mappingColumnLabels.toList()}
                """.trimLineStartsAndConsequentBlankLines()
            )
        }
    }

    private fun sanityCheckLoadedSourceRecordCount(
        loadedSourceRecordCount: Int,
        sourceTableDescriptors: List<Any>,
        dbConnection: DbConnection,
        sectionOutline: SectionOutline
    ) {
        val tableRowCountQueries = sourceTableDescriptors.map {
            val sb = StringBuilder()
            sb.append("SELECT COUNT(*) AS Count")

            when (it) {
                is String -> {
                    sb.append("\nFROM $it")
                }

                is SourceTableDescriptor -> {
                    sb.append("\nFROM ${it.table}")

                    if (it.joins != null) {
                        it.joins.forEach { join ->
                            sb.append("\nLEFT JOIN $join")
                        }
                    }

                    if (it.where != null) {
                        sb.append("\nWHERE ${it.where}")
                    }
                }

                else -> thisShouldNeverHappen("Unsupported SourceTable in ${this::class.simpleName}")
            }

            sb.toString()
        }

        val totalRowCountQuery = """
            SELECT SUM(Count) As TotalCount
            FROM (
            ${tableRowCountQueries.joinToString(separator = "\nUNION ALL\n")}
            )
        """.trimLineStartsAndConsequentBlankLines()

        val queryDebugName = "${sectionOutline.sectionTitle} TotalRowCount"

        val totalRowCount = dbConnection.executeQuery(totalRowCountQuery, queryDebugName) { resultSet ->
            resultSet.next()
            resultSet.getInt("TotalCount")
        }

        if (loadedSourceRecordCount != totalRowCount) {
            diagnostic.fatal(
                """
                Count mismatch in $queryDebugName, database: ${dbConnection.dbPath}".
                Loaded SourceRecords: $loadedSourceRecordCount
                SourceTable(s) total rows: $totalRowCount
                """.trimLineStartsAndConsequentBlankLines()
            )
        }
    }
}
