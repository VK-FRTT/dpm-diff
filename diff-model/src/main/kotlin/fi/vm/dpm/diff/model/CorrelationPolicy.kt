package fi.vm.dpm.diff.model

open class CorrelationPolicy(
    baselineSourceRecords: List<SourceRecord>,
    currentSourceRecords: List<SourceRecord>
) {

    companion object {

        fun create(
            sectionOutline: SectionOutline,
            baselineSourceRecords: List<SourceRecord>,
            currentSourceRecords: List<SourceRecord>
        ): CorrelationPolicy {

            return when (sectionOutline.sectionCorrelationMode) {
                CorrelationMode.DISTINCT_OBJECTS -> {
                    CorrelationPolicy(
                        baselineSourceRecords = baselineSourceRecords,
                        currentSourceRecords = currentSourceRecords
                    )
                }

                CorrelationMode.DISTINCT_SUB_OBJECTS -> {
                    CorrelationPolicySubObject(
                        baselineSourceRecords = baselineSourceRecords,
                        currentSourceRecords = currentSourceRecords

                    )
                }
            }
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

    open fun correlatingRecordPairs(): List<SourceRecordPair> {

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

    open fun duplicateCorrelationKeyRecords(): List<SourceRecord> {
        return recordsWithDuplicateKey(baselineRecordsByFullKey) +
            recordsWithDuplicateKey(currentRecordsByFullKey)
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

    private fun recordsWithDuplicateKey(
        pivotRecords: Map<CorrelationKey, List<SourceRecord>>
    ): List<SourceRecord> {

        return pivotRecords
            .mapNotNull { (_, pivotRecordGroup) ->
                if (pivotRecordGroup.size > 1) {
                    pivotRecordGroup
                } else {
                    null
                }
            }.flatten()
    }
}
