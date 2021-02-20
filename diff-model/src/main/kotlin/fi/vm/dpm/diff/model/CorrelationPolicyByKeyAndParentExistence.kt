package fi.vm.dpm.diff.model

class CorrelationPolicyByKeyAndParentExistence(
    baselineSourceRecords: List<SourceRecord>,
    currentSourceRecords: List<SourceRecord>
) : CorrelationPolicyByKey(
    baselineSourceRecords,
    currentSourceRecords
) {
    private val baselineRecordsByParentKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        baselineSourceRecords.groupBy { record -> record.parentKeyFieldKey }
    }

    private val currentRecordsByParentKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        currentSourceRecords.groupBy { record -> record.parentKeyFieldKey }
    }

    override fun deletedRecords(): List<SourceRecord> {
        val records = super.deletedRecords()

        return keepRecordsWhichParentExistsAndHaveNonNullPrimeKey(
            records = records,
            comparisonRecords = currentRecordsByParentKey
        )
    }

    override fun addedRecords(): List<SourceRecord> {
        val records = super.addedRecords()

        return keepRecordsWhichParentExistsAndHaveNonNullPrimeKey(
            records = records,
            comparisonRecords = baselineRecordsByParentKey
        )
    }

    private fun keepRecordsWhichParentExistsAndHaveNonNullPrimeKey(
        records: List<SourceRecord>,
        comparisonRecords: Map<CorrelationKey, List<SourceRecord>>
    ): List<SourceRecord> {
        return records.filter { record ->
            comparisonRecords[record.parentKeyFieldKey] != null &&
                !record.isPrimeKeyCompletelyNull()
        }
    }
}
