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

    protected fun executeChangeDetectionTest(
        baselineRecordsValues: String?,
        currentRecordsValues: String?,
        expectedResultsValues: String?,
        sectionOutline: SectionOutline,
        recordValueMapper: (List<String?>) -> (Map<Field, String?>),
        changeResultsMapper: (List<ChangeRecord>) -> (List<String>)
    ) {
        val changes = executeResolveChanges(
            sectionOutline = sectionOutline,
            baselineRecordsFieldValues = buildRecordsFieldValues(baselineRecordsValues, recordValueMapper),
            currentRecordsFieldValues = buildRecordsFieldValues(currentRecordsValues, recordValueMapper)
        )

        val changesResults = changeResultsMapper(changes)

        if (expectedResultsValues == null) {
            assertThat(changesResults).isEmpty()
        } else {
            val expectedResults = expectedResultsValues.splitExpectedResultsToList()
            assertThat(changesResults).containsExactly(*expectedResults.toTypedArray())
        }
    }

    protected fun List<ChangeRecord>.toKeyAndChangeKindList(): List<String> {
        return map { changeRecord ->
            "${CorrelationKey.createCorrelationKey(CorrelationKeyKind.FULL_KEY_FIELD_CORRELATION_KEY, changeRecord.fields).keyValue()} ${changeRecord.fields[changeKind]}"
        }
    }

    private fun buildRecordsFieldValues(
        recordsValues: String?,
        recordValueMapper: (List<String?>) -> (Map<Field, String?>)
    ): List<Map<Field, String?>> {
        if (recordsValues == null) return emptyList()

        return recordsValues
            .splitRecordsValuesToNestedLists()
            .map { recordValues -> recordValueMapper(recordValues) }
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
                sectionOutline,
                sourceKind,
                totalFieldValues
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
