package fi.vm.dpm.diff.model

import org.assertj.core.api.Assertions.assertThat

internal open class ChangeDetectionTestBase {

    protected val contextParentKey = KeyField(
        fieldName = "contextParentKey",
        keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
        keyFieldFallback = null
    )

    protected val contextParentKey2 = KeyField(
        fieldName = "contextParentKey2",
        keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
        keyFieldFallback = null
    )

    protected val parentKey = KeyField(
        fieldName = "parentKeyFieldKey",
        keyFieldKind = KeyFieldKind.PARENT_KEY,
        keyFieldFallback = null
    )

    protected val parentKey2 = KeyField(
        fieldName = "parentKey2",
        keyFieldKind = KeyFieldKind.PARENT_KEY,
        keyFieldFallback = null
    )

    protected val primeKey = KeyField(
        fieldName = "primeKey",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    protected val primeKey2 = KeyField(
        fieldName = "primeKey2",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    protected val atom = AtomField(
        fieldName = "Atom"
    )

    protected val changeKind = ChangeKindField()

    protected val note = NoteField()

    protected val idFallbackField = FallbackField(
        fieldName = "ID"
    )

    protected fun executeChangeDetectionTest(
        baselineRecordsValues: String?,
        currentRecordsValues: String?,
        expectedResultsValues: String?,
        sectionOutline: SectionOutline,
        recordValueMapper: (List<String?>) -> (Map<Field, String?>),
        changeRecordMapper: (ChangeRecord) -> (String)
    ) {
        val changeRecords = executeResolveChanges(
            sectionOutline = sectionOutline,
            baselineRecordsFieldValues = buildRecordsFieldValues(baselineRecordsValues, recordValueMapper),
            currentRecordsFieldValues = buildRecordsFieldValues(currentRecordsValues, recordValueMapper)
        )

        val verifiableResults = changeRecords.map(changeRecordMapper)

        if (expectedResultsValues == null) {
            assertThat(verifiableResults).isEmpty()
        } else {
            val expectedResults = expectedResultsValues.splitExpectedResultsToList()
            assertThat(verifiableResults).containsExactly(*expectedResults.toTypedArray())
        }
    }

    protected fun ChangeRecord.toKeyFieldsAndChangeKindString(): String {
        val fullKeyFieldCorrelationKeyValue = CorrelationKey
            .createCorrelationKey(
                CorrelationKeyKind.FULL_KEY_FIELD_CORRELATION_KEY,
                fields
            ).keyValue()

        val changeKindValue = fields[changeKind]

        return "$fullKeyFieldCorrelationKeyValue $changeKindValue"
    }

    protected fun ChangeRecord.toKeyFieldsAndIdFallbackAndChangeKindString(): String {
        val fullKeyFieldCorrelationKeyValue = CorrelationKey
            .createCorrelationKey(
                CorrelationKeyKind.FULL_KEY_FIELD_CORRELATION_KEY,
                fields
            ).keyValue()

        val idFallbackValue = fields[idFallbackField]

        val changeKindValue = fields[changeKind]

        return "$fullKeyFieldCorrelationKeyValue ($idFallbackValue) $changeKindValue"
    }

    private fun buildRecordsFieldValues(
        recordsValues: String?,
        recordValueMapper: (List<String?>) -> (Map<Field, String?>)
    ): List<Map<Field, String?>> {
        if (recordsValues == null) return emptyList()

        return recordsValues
            .splitRecordsValuesToNestedLists()
            .map { recordValues ->
                recordValueMapper(recordValues).map { (field, value) ->
                    val finalValue = if (value == "!null") {
                        null
                    } else {
                        value
                    }
                    field to finalValue
                }.toMap()
            }
    }

    private fun executeResolveChanges(
        sectionOutline: SectionOutline,
        baselineRecordsFieldValues: List<Map<Field, String?>>,
        currentRecordsFieldValues: List<Map<Field, String?>>
    ): List<ChangeRecord> {

        val baselineSourceRecords = createSourceRecords(
            sectionOutline,
            SourceKind.BASELINE,
            baselineRecordsFieldValues
        )

        val currentSourceRecords = createSourceRecords(
            sectionOutline,
            SourceKind.CURRENT,
            currentRecordsFieldValues
        )

        val changes = ChangeRecord.resolveChanges(
            sectionOutline = sectionOutline,
            baselineSourceRecords = baselineSourceRecords,
            currentSourceRecords = currentSourceRecords
        )

        return changes
    }

    private fun createSourceRecords(
        sectionOutline: SectionOutline,
        sourceKind: SourceKind,
        recordsFieldValues: List<Map<Field, String?>>
    ): List<SourceRecord> {
        val sourceRecords = recordsFieldValues.map { recordFieldValues ->

            val totalFieldValues = sectionOutline.sectionFields.map {
                it to null
            }.toMap() + recordFieldValues

            SourceRecord(
                totalFieldValues,
                sectionOutline,
                sourceKind
            )
        }

        return sourceRecords
    }

    private fun String.splitRecordsValuesToNestedLists(): List<List<String>> {
        val recordsList = split("|").map { it.trim() }
        return recordsList.map { record -> record.split(" ").map { it.trim() } }
    }

    private fun String.splitExpectedResultsToList(): List<String> {
        return split("|").map { it.trim() }
    }
}
