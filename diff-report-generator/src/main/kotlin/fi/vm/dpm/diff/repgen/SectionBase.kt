package fi.vm.dpm.diff.repgen

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.ChangeRecord
import fi.vm.dpm.diff.model.CorrelationMode
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.ReportSection
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.model.SourceBundle
import fi.vm.dpm.diff.model.SourceRecord
import fi.vm.dpm.diff.model.thisShouldNeverHappen
import fi.vm.dpm.diff.repgen.section.SourceTableDescriptor
import java.sql.ResultSet

open class SectionBase(
    private val generationContext: GenerationContext
) {
    fun sanityCheckSectionConfig() {
        sectionDescriptor.sanityCheck()
    }

    protected open val identificationLabels: Array<FieldDescriptor> = emptyArray()

    protected val changeKind = FieldDescriptor(
        fieldKind = FieldKind.CHANGE_KIND,
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
        sectionFields = emptyList(),
        correlationMode = CorrelationMode.UNINITIALIZED,
        includedChanges = emptySet()
    )

    protected open val queryColumnMapping: Map<String, FieldDescriptor> = emptyMap()

    protected open val query: String = ""

    protected open val sourceTableDescriptors: List<Any> = emptyList()

    fun idLabelFields(
        fieldNameBase: String,
        noteField: FieldDescriptor
    ): Array<FieldDescriptor> {
        return generationContext.identificationLabelLangCodes.map { langCode ->
            FieldDescriptor(
                fieldKind = FieldKind.IDENTIFICATION_LABEL,
                fieldName = "$fieldNameBase${langCode.toUpperCase()}",
                noteFields = listOf(noteField)
            )
        }.toTypedArray()
    }

    fun idLabelColumnMapping(): Array<Pair<String, FieldDescriptor>> {
        return generationContext.identificationLabelLangCodes.mapIndexed { index, langCode ->
            val field = identificationLabels[index]
            val columnName = idLabelColumnName(langCode)
            Pair(columnName, field)
        }.toTypedArray()
    }

    fun idLabelColumnNamesFragment(): String {
        return generationContext.identificationLabelLangCodes.map { langCode ->
            """
             ,${idLabelColumnName(langCode)} AS '${idLabelColumnName(langCode)}'
            """.trimLineStartsAndConsequentBlankLines()
        }.joinToString(
            separator = "\n"
        )
    }

    fun idLabelAggregateFragment(): String {
        return generationContext.identificationLabelLangCodes.map { langCode ->
            """
            ,MAX(CASE WHEN mLanguage.IsoCode = '$langCode' THEN mConceptTranslation.Text END) AS ${idLabelColumnName(
                langCode
            )}
            """.trimLineStartsAndConsequentBlankLines()
        }.joinToString(separator = "\n")
    }

    private fun idLabelColumnName(langCode: String): String {
        return "IdLabel${langCode.toUpperCase()}"
    }

    fun generateSection(): ReportSection {
        generationContext.diagnostic.info("Section: ${sectionDescriptor.sectionTitle}")
        sectionDescriptor.sanityCheck()

        val baselineBundle = SourceBundle(
            sectionDescriptor,
            loadSourceRecords(generationContext.baselineConnection)
        )

        val actualBundle = SourceBundle(
            sectionDescriptor,
            loadSourceRecords(generationContext.actualConnection)
        )

        val changes = ChangeRecord.resolveChanges(
            sectionDescriptor = sectionDescriptor,
            baselineBundle = baselineBundle,
            actualBundle = actualBundle
        )

        return ReportSection(
            sectionDescriptor = sectionDescriptor,
            changes = changes
        )
    }

    private fun loadSourceRecords(
        dbConnection: DbConnection
    ): List<SourceRecord> {
        val sourceRecords = mutableListOf<SourceRecord>()

        dbConnection.executeQuery(query) { resultSet ->
            sanityCheckResultSetColumnLabels(resultSet)

            while (resultSet.next()) {

                val loadedFields = queryColumnMapping.map { (column, field) ->
                    field to resultSet.getString(column)
                }.toMap()

                val sourceRecord = SourceRecord(
                    sectionDescriptor = sectionDescriptor,
                    fields = loadedFields
                )

                sourceRecords.add(sourceRecord)
            }
        }

        sanityCheckLoadedSourceRecordsCount(
            sourceRecords,
            dbConnection
        )

        return sourceRecords
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
        val tableRowCountQueries = sourceTableDescriptors.map {
            val sb = StringBuilder()
            sb.append("SELECT COUNT(*) AS Count")

            when (it) {
                is String -> {
                    sb.append("\nFROM $it")
                }

                is SourceTableDescriptor -> {
                    sb.append("\nFROM ${it.table}")

                    if (it.where != null) {
                        sb.append("\nWHERE ${it.where}")
                    }

                    if (it.join != null) {
                        sb.append("\nLEFT JOIN ${it.join}")
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

        val totalRowCount = dbConnection.executeQuery(totalRowCountQuery) { resultSet ->
            resultSet.next()
            resultSet.getInt("TotalCount")
        }

        if (loadedRecords.size != totalRowCount) {
            generationContext.diagnostic.fatal(
                """
                Count mismatch in ${sectionDescriptor.sectionTitle}, database: ${dbConnection.dbPath}".
                SourceRecords: ${loadedRecords.size}
                SourceTables total rows: $totalRowCount
                """.trimLineStartsAndConsequentBlankLines()
            )
        }
    }
}
