package fi.vm.dpm.diff.repgen

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.DifferenceRecord
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.ReportSection
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.model.SourceRecord

open class SectionBase(
    private val generationContext: GenerationContext
) {
    protected open val identificationLabels: Array<FieldDescriptor> = emptyArray()

    protected val differenceKind = FieldDescriptor(
        fieldKind = FieldKind.DIFFERENCE_KIND,
        fieldName = "change"
    )

    protected open val sectionDescriptor: SectionDescriptor = SectionDescriptor(
        sectionShortTitle = "",
        sectionTitle = "",
        sectionDescription = "",
        sectionFields = emptyList()
    )

    protected open val query: String = ""

    protected open val queryPrimaryTables: List<String> = emptyList()

    protected open val columnNames: Map<String, FieldDescriptor> = emptyMap()

    private val fieldsToColumnNames by lazy {
        columnNames.entries.associate { (columnMame, field) -> field to columnMame }
    }

    fun composeIdentificationLabels(
        nameCompose: (String) -> String
    ): Array<FieldDescriptor> {

        return generationContext.identificationLabelLangCodes.map { langCode ->

            FieldDescriptor(
                fieldKind = FieldKind.IDENTIFICATION_LABEL,
                fieldName = nameCompose(langCode)
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
        isoCodeColumnName: String,
        textColumnName: String
    ): String {
        return generationContext.identificationLabelLangCodes.map { langCode ->
            ",MAX(CASE WHEN $isoCodeColumnName = '$langCode' THEN $textColumnName END) AS 'IdLabel_$langCode'"
        }.joinToString(
            separator = "\n"
        )
    }

    fun generateSection(): ReportSection {
        generationContext.diagnostic.info("Section: ${sectionDescriptor.sectionTitle}")

        val baselineRecords = loadSourceRecords(generationContext.baselineConnection)
        val actualRecords = loadSourceRecords(generationContext.actualConnection)

        val differences = resolveDifferences(
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
        val sourceRecords = mutableSetOf<SourceRecord>()

        dbConnection.executeQuery(query) { resultSet ->
            while (resultSet.next()) {

                val loadedFields = sectionDescriptor.sectionFields.map { sectionField ->
                    val columnName = fieldsToColumnNames[sectionField]
                    if (columnName == null) {
                        sectionField to null
                    } else {
                        sectionField to resultSet.getString(columnName)
                    }
                }.toMap()

                val sourceRecord = SourceRecord(
                    fields = loadedFields
                )

                sourceRecords.add(sourceRecord)
            }
        }

        sanityCheckSourceRecordsCount(
            sourceRecords,
            dbConnection
        )

        return sourceRecords.map {
            it.correlationKey() to it
        }.toMap()
    }

    private fun sanityCheckSourceRecordsCount(
        sourceRecords: Set<SourceRecord>,
        dbConnection: DbConnection
    ) {
        val primaryTablesRowCountQuery = """
            SELECT SUM(Count) As TotalCount FROM (
            ${queryPrimaryTables.map { "SELECT COUNT(*) AS Count FROM $it" }.joinToString(separator = "\nUNION ALL\n")}
            )
        """.trimLineStartsAndConsequentBlankLines()

        val totalRowCount = dbConnection.executeQuery(primaryTablesRowCountQuery) { resultSet ->
            resultSet.next()
            resultSet.getInt("TotalCount")
        }

        if (sourceRecords.size != totalRowCount) {
            generationContext.diagnostic.fatal(
                "Count mismatch: SourceRecords ${sourceRecords.size}, PrimaryTables total rows $totalRowCount. " +
                    "Section: ${sectionDescriptor.sectionTitle}, Database: ${dbConnection.dbPath}"
            )
        }
    }

    private fun resolveDifferences(
        baselineRecords: Map<String, SourceRecord>,
        actualRecords: Map<String, SourceRecord>
    ): List<DifferenceRecord> {

        val added = actualRecords
            .filterRecordsHavingNoCorrelationIn(baselineRecords)
            .map { it.toAddedDifference() }

        val removed = baselineRecords
            .filterRecordsHavingNoCorrelationIn(actualRecords)
            .map { it.toRemovedDifference() }

        val changed = actualRecords
            .filterCorrelatingRecords(baselineRecords)
            .mapNotNull { (actualRecord, baselineRecord) ->
                actualRecord.toChangedDifferenceOrNull(baselineRecord)
            }

        return added + removed + changed
    }

    private fun Map<String, SourceRecord>.filterRecordsHavingNoCorrelationIn(
        otherRecords: Map<String, SourceRecord>
    ): List<SourceRecord> {
        return mapNotNull { (correlationKey, record) ->
            if (otherRecords.containsKey(correlationKey)) {
                null
            } else {
                record
            }
        }
    }

    private fun Map<String, SourceRecord>.filterCorrelatingRecords(
        otherRecords: Map<String, SourceRecord>
    ): List<Pair<SourceRecord, SourceRecord>> {
        return mapNotNull { (correlationKey, primaryRecord) ->
            val correlatingRecord = otherRecords[correlationKey]

            if (correlatingRecord == null) {
                null
            } else {
                Pair(primaryRecord, correlatingRecord)
            }
        }
    }
}
