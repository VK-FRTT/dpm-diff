package fi.vm.dpm.diff.model

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.SQLiteDbConnection
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.utils.SourceTableDescriptor
import java.io.Closeable
import java.nio.file.Path
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SqlReportGenerator(
    private val sectionPlans: Collection<SectionPlanSql>,
    private val reportGeneratorDescriptor: ReportGeneratorDescriptor,
    private val reportGenerationOptions: List<String>,
    private val baselineDbPath: Path,
    private val currentDbPath: Path,
    private val diagnostic: Diagnostic
) : Closeable {

    private val baselineConnection: SQLiteDbConnection by lazy {
        SQLiteDbConnection(baselineDbPath, diagnostic)
    }

    private val currentConnection: SQLiteDbConnection by lazy {
        SQLiteDbConnection(currentDbPath, diagnostic)
    }

    override fun close() {
        baselineConnection.close()
        currentConnection.close()
    }

    fun generateReport(): ChangeReport {

        val reportSections = sectionPlans.map {
            generateSection(it)
        }

        return ChangeReport(
            reportKind = ChangeReportKind.DPM,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")),
            baselineFileName = baselineDbPath.fileName.toString(),
            currentFileName = currentDbPath.fileName.toString(),
            sections = reportSections,
            reportGeneratorDescriptor = reportGeneratorDescriptor,
            reportGenerationOptions = reportGenerationOptions
        )
    }

    private fun generateSection(sectionPlan: SectionPlanSql): ReportSection {
        diagnostic.info("Section: ${sectionPlan.sectionOutline.sectionTitle}")
        sectionPlan.sectionOutline.sanityCheck()

        val baselineSourceRecords = loadSourceRecords(
            sectionPlan,
            baselineConnection,
            SourceKind.BASELINE
        )

        sanityCheckLoadedSourceRecordsCount(
            sectionPlan,
            baselineSourceRecords,
            baselineConnection,
            SourceKind.BASELINE
        )

        val currentSourceRecords = loadSourceRecords(
            sectionPlan,
            currentConnection,
            SourceKind.CURRENT
        )

        sanityCheckLoadedSourceRecordsCount(
            sectionPlan,
            currentSourceRecords,
            currentConnection,
            SourceKind.CURRENT
        )

        val changes = ChangeRecord.resolveChanges(
            sectionOutline = sectionPlan.sectionOutline,
            baselineSourceRecords = baselineSourceRecords,
            currentSourceRecords = currentSourceRecords
        )

        diagnostic.info("... changes: ${changes.size}")

        return ReportSection(
            sectionOutline = sectionPlan.sectionOutline,
            changes = changes
        )
    }

    private fun loadSourceRecords(
        sectionPlan: SectionPlanSql,
        dbConnection: SQLiteDbConnection,
        sourceKind: SourceKind
    ): List<SourceRecord> {
        val sourceRecords = mutableListOf<SourceRecord>()
        val queryName = "${sectionPlan.sectionOutline.sectionShortTitle} $sourceKind Records"

        dbConnection.executeQuery(sectionPlan.query, queryName) { resultSet ->

            sanityCheckResultSetColumnLabels(
                sectionPlan,
                resultSet
            )

            while (resultSet.next()) {

                val loadedFields = sectionPlan.queryColumnMapping.map { (column, field) ->
                    field to resultSet.getString(column)
                }.toMap()

                val sourceRecord = SourceRecord(
                    sectionOutline = sectionPlan.sectionOutline,
                    sourceKind = sourceKind,
                    fields = loadedFields
                )

                sourceRecords.add(sourceRecord)
            }
        }

        return sourceRecords
    }

    private fun sanityCheckResultSetColumnLabels(
        sectionPlan: SectionPlanSql,
        resultSet: ResultSet
    ) {
        val resultSetColumnLabels =
            (1..resultSet.metaData.columnCount)
                .map { resultSet.metaData.getColumnLabel(it) }
                .toTypedArray()

        val mappingColumnLabels =
            sectionPlan.queryColumnMapping
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
        sectionPlan: SectionPlanSql,
        loadedRecords: List<SourceRecord>,
        dbConnection: SQLiteDbConnection,
        sourceKind: SourceKind
    ) {
        val tableRowCountQueries = sectionPlan.sourceTableDescriptors.map {
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

        val queryName = "${sectionPlan.sectionOutline.sectionTitle} $sourceKind TotalRowCounts"

        val totalRowCount = dbConnection.executeQuery(totalRowCountQuery, queryName) { resultSet ->
            resultSet.next()
            resultSet.getInt("TotalCount")
        }

        if (loadedRecords.size != totalRowCount) {
            diagnostic.fatal(
                """
                Count mismatch in $queryName, database: ${dbConnection.dbPath}".
                SourceRecords: ${loadedRecords.size}
                SourceTable(s) total rows: $totalRowCount
                """.trimLineStartsAndConsequentBlankLines()
            )
        }
    }
}
