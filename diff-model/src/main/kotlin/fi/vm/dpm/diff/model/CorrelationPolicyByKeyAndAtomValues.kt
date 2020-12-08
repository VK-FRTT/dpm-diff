package fi.vm.dpm.diff.model

open class CorrelationPolicyByKeyAndAtomValues(
    baselineSourceRecords: List<SourceRecord>,
    currentSourceRecords: List<SourceRecord>
) : CorrelationPolicy {

    private val baselineRecordsByFullKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        baselineSourceRecords.groupBy { record -> record.fullKey }
    }

    private val currentRecordsByFullKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        currentSourceRecords.groupBy { record -> record.fullKey }
    }

    override fun deletedRecords(): List<SourceRecord> {
        return baselineRecordsByFullKey
            .mapNotNull { (key, baselineRecordGroup) ->
                val currentRecordGroup = currentRecordsByFullKey[key]

                recordsWithoutCorrelation(
                    pivotRecords = baselineRecordGroup,
                    comparisonRecords = currentRecordGroup
                )
            }
            .flatten()
    }

    override fun addedRecords(): List<SourceRecord> {
        return currentRecordsByFullKey
            .mapNotNull { (key, currentRecordGroup) ->
                val baselineRecordGroup = baselineRecordsByFullKey[key]

                recordsWithoutCorrelation(
                    pivotRecords = currentRecordGroup,
                    comparisonRecords = baselineRecordGroup
                )
            }
            .flatten()
    }

    override fun correlatingRecordPairs(): List<SourceRecordPair> {
        thisShouldNeverHappen("Not supported")
    }

    override fun duplicateCorrelationKeyRecords(): List<SourceRecord> {
        thisShouldNeverHappen("Not supported")
    }

    private fun recordsWithoutCorrelation(
        pivotRecords: List<SourceRecord>,
        comparisonRecords: List<SourceRecord>?
    ): List<SourceRecord>? {

        if (comparisonRecords == null) {
            return pivotRecords
        }

        return emptyList()
    }
}
