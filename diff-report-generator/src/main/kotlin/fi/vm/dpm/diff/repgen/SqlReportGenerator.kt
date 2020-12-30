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

class SqlReportGenerator(
    private val sectionPlans: Collection<SectionPlanSql>,
    private val sourceDbs: SourceDbs,
    private val reportGeneratorDescriptor: ReportGeneratorDescriptor,
    private val reportGenerationOptions: List<String>,
    private val diagnostic: Diagnostic
) {
    private enum class ReportGenerationStep {
        LOAD_BASELINE,
        LOAD_CURRENT,
        RESOLVE_CHANGES,
        SORT_CHANGES
    }

    private val timeMetrics = TimeMetrics<ReportGenerationStep>()

    init {
        timeMetrics.createMetric(ReportGenerationStep.LOAD_BASELINE, "Loading baseline records")
        timeMetrics.createMetric(ReportGenerationStep.LOAD_CURRENT, "Loading current records")
        timeMetrics.createMetric(ReportGenerationStep.RESOLVE_CHANGES, "Resolving changes")
        timeMetrics.createMetric(ReportGenerationStep.SORT_CHANGES, "Sorting changes")
    }

    fun generateReport(): ChangeReport {

        val reportSections = sectionPlans.map {
            val section = generateSection(it)

            diagnostic.verbose(timeMetrics.report())
            timeMetrics.reset()

            section
        }

        return ChangeReport(
            reportKind = ChangeReportKind.DPM,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")),
            baselineFileName = sourceDbs.baselineDbPath.fileName.toString(),
            currentFileName = sourceDbs.currentDbPath.fileName.toString(),
            sections = reportSections,
            reportGeneratorDescriptor = reportGeneratorDescriptor,
            reportGenerationOptions = reportGenerationOptions
        )
    }

    private fun generateSection(sectionPlan: SectionPlanSql): ReportSection {
        diagnostic.info("\nSection: ${sectionPlan.sectionOutline().sectionTitle}")
        sectionPlan.sanityCheck()

        val partitionedQueries = sectionPlan.partitionedQueries()

        val partitionResults = partitionedQueries.mapIndexed { partitionIndex, partitionQuery ->

            val baselineSourceRecords = runStep(ReportGenerationStep.LOAD_BASELINE) {
                loadSourceRecordsPartition(
                    partitionQuery = partitionQuery,
                    partitionIndex = partitionIndex,
                    totalPartitions = partitionedQueries.size,
                    queryColumnMapping = sectionPlan.queryColumnMapping(),
                    sectionOutline = sectionPlan.sectionOutline(),
                    dbConnection = sourceDbs.baselineConnection
                )
            }

            val currentSourceRecords = runStep(ReportGenerationStep.LOAD_CURRENT) {
                loadSourceRecordsPartition(
                    partitionQuery = partitionQuery,
                    partitionIndex = partitionIndex,
                    totalPartitions = partitionedQueries.size,
                    queryColumnMapping = sectionPlan.queryColumnMapping(),
                    sectionOutline = sectionPlan.sectionOutline(),
                    dbConnection = sourceDbs.currentConnection
                )
            }

            val changes = runStep(ReportGenerationStep.RESOLVE_CHANGES) {
                ChangeRecord.resolveChanges(
                    sectionOutline = sectionPlan.sectionOutline(),
                    baselineSourceRecords = baselineSourceRecords,
                    currentSourceRecords = currentSourceRecords
                )
            }

            PartitionResult(
                changes = changes,
                baselineSourceRecordCount = baselineSourceRecords.size,
                currentSourceRecordCount = currentSourceRecords.size
            )
        }

        sanityCheckLoadedSourceRecordsCount(
            partitionResults.sumBy { it.baselineSourceRecordCount },
            sectionPlan.sourceTableDescriptors(),
            sourceDbs.baselineConnection,
            sectionPlan.sectionOutline()
        )

        sanityCheckLoadedSourceRecordsCount(
            partitionResults.sumBy { it.currentSourceRecordCount },
            sectionPlan.sourceTableDescriptors(),
            sourceDbs.currentConnection,
            sectionPlan.sectionOutline()
        )

        val changes = runStep(ReportGenerationStep.SORT_CHANGES) {
            partitionResults
                .flatMap { it.changes }
                .sortedWith(ChangeRecordComparator(sectionPlan.sectionOutline().sectionSortOrder))
        }

        diagnostic.info(" => changes: ${changes.size}")

        return ReportSection(
            sectionOutline = sectionPlan.sectionOutline(),
            changes = changes
        )
    }

    private fun <T> runStep(step: ReportGenerationStep, action: () -> T): T {
        diagnostic.infoStepProgress()
        timeMetrics.startStep(step)

        val result = action()

        timeMetrics.stopStep(step)

        return result
    }

    private fun loadSourceRecordsPartition(
        partitionQuery: String,
        partitionIndex: Int,
        totalPartitions: Int,
        queryColumnMapping: Map<String, Field>,
        sectionOutline: SectionOutline,
        dbConnection: DbConnection
    ): List<SourceRecord> {
        val sourceRecords = mutableListOf<SourceRecord>()

        val queryDebugName =
            "${sectionOutline.sectionTitle} SourceRecordsPartition ${partitionIndex + 1}/$totalPartitions"

        dbConnection.executeQuery(partitionQuery, queryDebugName) { resultSet ->

            sanityCheckResultSetColumnLabels(
                queryColumnMapping,
                resultSet
            )

            while (resultSet.next()) {

                val loadedFields = queryColumnMapping.map { (columnLabel, field) ->
                    field to resultSet.getString(columnLabel)
                }.toMap()

                val sourceRecord = SourceRecord(
                    sectionOutline = sectionOutline,
                    sourceKind = dbConnection.sourceKind,
                    fields = loadedFields
                )

                sourceRecords.add(sourceRecord)
            }
        }

        return sourceRecords
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

    private fun sanityCheckLoadedSourceRecordsCount(
        loadedRecordCount: Int,
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

        if (loadedRecordCount != totalRowCount) {
            diagnostic.fatal(
                """
                Count mismatch in $queryDebugName, database: ${dbConnection.dbPath}".
                Loaded SourceRecords: $loadedRecordCount
                SourceTable(s) total rows: $totalRowCount
                """.trimLineStartsAndConsequentBlankLines()
            )
        }
    }

    data class PartitionResult(
        val changes: List<ChangeRecord>,
        val baselineSourceRecordCount: Int,
        val currentSourceRecordCount: Int
    )
}
