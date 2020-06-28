package fi.vm.dpm.diff.model

import org.assertj.core.api.Assertions.assertThat

internal open class ChangeDetectionTestBase {

    protected val scopingTopLevelKeySegment = KeySegmentField(
        fieldName = "scopingTopLevelKeySegment",
        segmentKind = KeySegmentKind.SCOPING_TOP_LEVEL_SEGMENT,
        segmentFallback = null
    )

    protected val scopingTopLevelKeySegment2 = KeySegmentField(
        fieldName = "scopingTopLevelKeySegment",
        segmentKind = KeySegmentKind.SCOPING_TOP_LEVEL_SEGMENT,
        segmentFallback = null
    )

    protected val topLevelKeySegment = KeySegmentField(
        fieldName = "topLevelKeySegment",
        segmentKind = KeySegmentKind.TOP_LEVEL_SEGMENT,
        segmentFallback = null
    )

    protected val topLevelKeySegment2 = KeySegmentField(
        fieldName = "topLevelKeySegment2",
        segmentKind = KeySegmentKind.TOP_LEVEL_SEGMENT,
        segmentFallback = null
    )

    protected val subObjectSegment = KeySegmentField(
        fieldName = "subObjectSegment",
        segmentKind = KeySegmentKind.SUB_OBJECT_SEGMENT,
        segmentFallback = null
    )

    protected val subObjectSegment2 = KeySegmentField(
        fieldName = "subObjectSegment2",
        segmentKind = KeySegmentKind.SUB_OBJECT_SEGMENT,
        segmentFallback = null
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
        sectionDescriptor: SectionDescriptor,
        recordValueMapper: (List<String?>) -> (Map<Field, String?>),
        changeResultsMapper: (List<ChangeRecord>) -> (List<String>)
    ) {
        val changes = executeResolveChanges(
            sectionDescriptor = sectionDescriptor,
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
            "${CorrelationKey.fullKey(changeRecord.fields).keyValue()} ${changeRecord.fields[changeKind]}"
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
        sectionDescriptor: SectionDescriptor,
        baselineRecordsFieldValues: List<Map<Field, String?>>,
        currentRecordsFieldValues: List<Map<Field, String?>>
    ): List<ChangeRecord> {

        val baselineBundle = createSourceBundle(
            sectionDescriptor,
            SourceKind.BASELINE,
            baselineRecordsFieldValues
        )

        val currentBundle = createSourceBundle(
            sectionDescriptor,
            SourceKind.CURRENT,
            currentRecordsFieldValues
        )

        val changes = ChangeRecord.resolveChanges(
            sectionDescriptor = sectionDescriptor,
            baselineBundle = baselineBundle,
            currentBundle = currentBundle
        )

        return changes
    }

    private fun createSourceBundle(
        sectionDescriptor: SectionDescriptor,
        sourceKind: SourceKind,
        recordsFieldValues: List<Map<Field, String?>>
    ): SourceBundle {

        val sourceRecords = recordsFieldValues.map { recordFieldValues ->

            val totalFieldValues = sectionDescriptor.sectionFields.map {
                it to null
            }.toMap() + recordFieldValues

            SourceRecord(
                sectionDescriptor,
                sourceKind,
                totalFieldValues
            )
        }

        return SourceBundle(
            sectionDescriptor = sectionDescriptor,
            sourceRecords = sourceRecords
        )
    }

    private fun String.splitRecordsValuesToNestedLists(): List<List<String>> {
        val recordsList = split("|").map { it.trim() }
        return recordsList.map { record -> record.split(" ").map { it.trim() } }
    }

    private fun String.splitExpectedResultsToList(): List<String> {
        return split("|").map { it.trim() }
    }
}
