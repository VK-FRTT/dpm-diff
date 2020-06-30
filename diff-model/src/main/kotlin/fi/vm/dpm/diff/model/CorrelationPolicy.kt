package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType

open class CorrelationPolicy(
    baselineSourceRecords: List<SourceRecord>,
    currentSourceRecords: List<SourceRecord>
) {

    companion object {

        fun create(
            sectionDescriptor: SectionDescriptor,
            baselineSourceRecords: List<SourceRecord>,
            currentSourceRecords: List<SourceRecord>
        ): CorrelationPolicy {

            return if (sectionDescriptor
                    .sectionFields
                    .filterFieldType<KeySegmentField>()
                    .any { it.segmentKind == KeySegmentKind.SUB_SEGMENT }
            ) {
                CorrelationPolicySubObject(
                    baselineSourceRecords = baselineSourceRecords,
                    currentSourceRecords = currentSourceRecords

                )
            } else
                CorrelationPolicy(
                    baselineSourceRecords = baselineSourceRecords,
                    currentSourceRecords = currentSourceRecords
                )
        }
    }

    private val baselineRecordsByFullKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        baselineSourceRecords.groupBy { record -> record.fullKey }
    }

    private val currentRecordsByFullKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        currentSourceRecords.groupBy { record -> record.fullKey }
    }

    open fun deletedRecords(): List<SourceRecord> {
        return recordsWithOneToNoneCorrelation(
            pivotRecords = baselineRecordsByFullKey,
            comparisonRecords = currentRecordsByFullKey
        )
    }

    open fun addedRecords(): List<SourceRecord> {
        return recordsWithOneToNoneCorrelation(
            pivotRecords = currentRecordsByFullKey,
            comparisonRecords = baselineRecordsByFullKey
        )
    }

    open fun sameIdentityRecordPairs(): List<SourceRecordPair> {

        return currentRecordsByFullKey
            .mapNotNull { (key, currentRecordGroup) ->

                val baselineRecordGroup = baselineRecordsByFullKey[key]

                if (currentRecordGroup.size == 1 &&
                    baselineRecordGroup != null &&
                    baselineRecordGroup.size == 1
                ) {
                    SourceRecordPair(
                        currentRecord = currentRecordGroup.first(),
                        baselineRecord = baselineRecordGroup.first()
                    )
                } else {
                    null
                }
            }
    }

    private fun recordsWithOneToNoneCorrelation(
        pivotRecords: Map<CorrelationKey, List<SourceRecord>>,
        comparisonRecords: Map<CorrelationKey, List<SourceRecord>>
    ): List<SourceRecord> {

        return pivotRecords
            .mapNotNull { (pivotKey, pivotRecordGroup) ->
                val comparisonRecordGroup = comparisonRecords[pivotKey]

                if (pivotRecordGroup.size == 1 &&
                    comparisonRecordGroup == null
                ) {
                    pivotRecordGroup.first()
                } else {
                    null
                }
            }
    }
}
