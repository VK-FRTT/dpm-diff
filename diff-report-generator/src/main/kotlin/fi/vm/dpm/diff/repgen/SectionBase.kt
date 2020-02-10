package fi.vm.dpm.diff.repgen

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.DifferenceRecord
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.ReportSection
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.model.SourceRecord
import fi.vm.dpm.diff.model.thisShouldNeverHappen
import java.sql.ResultSet

open class SectionBase(
    private val generationContext: GenerationContext
) {
    protected open val identificationLabels: Array<FieldDescriptor> = emptyArray()

    protected val differenceKind = FieldDescriptor(
        fieldKind = FieldKind.DIFFERENCE_KIND,
        fieldName = "Change"
    )

    protected val note = FieldDescriptor(
        fieldKind = FieldKind.NOTE,
        fieldName = "Notes"
    )

    protected open val sectionDescriptor: SectionDescriptor = SectionDescriptor(
        sectionShortTitle = "",
        sectionTitle = "",
        sectionDescription = "",
        sectionFields = emptyList()
    )

    protected open val query: String = ""

    protected open val primaryTables: List<Any> = emptyList()

    protected open val queryColumnMapping: Map<String, FieldDescriptor> = emptyMap()

    fun composeIdentificationLabelFields(
        noteFallback: FieldDescriptor,
        composeName: (String) -> String
    ): Array<FieldDescriptor> {

        return generationContext.identificationLabelLangCodes.map { langCode ->

            FieldDescriptor(
                fieldKind = FieldKind.IDENTIFICATION_LABEL,
                fieldName = composeName(langCode),
                noteFallback = listOf(noteFallback)
            )
        }.toTypedArray()
    }

    fun composeIdentificationLabelColumnNames(): Array<Pair<String, FieldDescriptor>> {

        return generationContext.identificationLabelLangCodes.mapIndexed { index, langCode ->

            val field = identificationLabels[index]
            Pair("IdLabel_$langCode", field)
        }.toTypedArray()
    }

    fun composeIdentificationLabelQueryFragment(
        criteriaLangColumn: String,
        sourceTextColumn: String
    ): String {

        return generationContext.identificationLabelLangCodes.map { langCode ->
            ",MAX(CASE WHEN $criteriaLangColumn = '$langCode' THEN $sourceTextColumn END) AS 'IdLabel_$langCode'"
        }.joinToString(
            separator = "\n"
        )
    }

    fun generateSection(): ReportSection {
        generationContext.diagnostic.info("Section: ${sectionDescriptor.sectionTitle}")
        sanityCheckSectionFieldsConfig()

        val baselineRecords = loadSourceRecords(generationContext.baselineConnection)
        val actualRecords = loadSourceRecords(generationContext.actualConnection)

        val differences = DifferenceRecord.resolveDifferences(
            baselineRecords = baselineRecords,
            actualRecords = actualRecords
        )

        return ReportSection(
            sectionDescriptor = sectionDescriptor,
            differences = differences
        )
    }

    private fun loadSourceRecords(
        dbConnection: DbConnection
    ): Map<String, SourceRecord> {
        val sourceRecords = mutableListOf<SourceRecord>()

        dbConnection.executeQuery(query) { resultSet ->
            sanityCheckResultSetColumnLabels(resultSet)

            while (resultSet.next()) {

                val loadedFields = queryColumnMapping.map { (column, field) ->
                    field to resultSet.getString(column)
                }.toMap()

                val sourceRecord = SourceRecord(
                    sectionFields = sectionDescriptor.sectionFields,
                    fields = loadedFields
                )

                sourceRecords.add(sourceRecord)
            }
        }

        sanityCheckLoadedSourceRecordsCount(
            sourceRecords,
            dbConnection
        )

        return sourceRecords.map {
            it.correlationKey() to it
        }.toMap()
    }

    private fun sanityCheckSectionFieldsConfig() {
        // TODO
        // Max counts per restricted FieldKind
        // Fallback fields refer only to Fallback kinds
        // Fallbacks are used only in CorrelationKeys
    }

    private fun sanityCheckResultSetColumnLabels(resultSet: ResultSet) {
        val resultSetColumnLabels =
            (1..resultSet.metaData.columnCount)
                .map { resultSet.metaData.getColumnLabel(it) }
                .toTypedArray()

        val mappingColumnLabels =
            queryColumnMapping
                .map { (columnLabel, _) -> columnLabel }
                .toTypedArray()

        if (!(resultSetColumnLabels contentDeepEquals mappingColumnLabels)) {
            generationContext.diagnostic.fatal(
                """
                ResultSet and ColumnMapping mismatch.
                ResultSet columns: ${resultSetColumnLabels.toList()}
                ColumnMapping columns: ${mappingColumnLabels.toList()}
                """.trimLineStartsAndConsequentBlankLines()
            )
        }
    }

    private fun sanityCheckLoadedSourceRecordsCount(
        loadedRecords: List<SourceRecord>,
        dbConnection: DbConnection
    ) {
        val rowCountQueries = primaryTables.map {
            when (it) {
                is String -> {
                    """
                    SELECT COUNT(*) AS Count
                    FROM $it
                    """.trimLineStartsAndConsequentBlankLines()
                }

                is Pair<*, *> -> {
                    """
                    SELECT COUNT(*) AS Count
                    FROM ${it.first}
                    WHERE ${it.second}
                    """.trimLineStartsAndConsequentBlankLines()
                }

                else -> thisShouldNeverHappen("Unsupported PrimaryTables")
            }
        }

        val primaryTablesRowCountQuery = """
            SELECT SUM(Count) As TotalCount FROM (
            ${rowCountQueries.joinToString(separator = "\nUNION ALL\n")}
            )
        """.trimLineStartsAndConsequentBlankLines()

        val totalRowCount = dbConnection.executeQuery(primaryTablesRowCountQuery) { resultSet ->
            resultSet.next()
            resultSet.getInt("TotalCount")
        }

        if (loadedRecords.size != totalRowCount) {
            generationContext.diagnostic.fatal(
                """
                Count mismatch in ${sectionDescriptor.sectionTitle}, database: ${dbConnection.dbPath}".
                SourceRecords: ${loadedRecords.size}
                PrimaryTables total rows: $totalRowCount
                """.trimLineStartsAndConsequentBlankLines()
            )
        }
    }
}
