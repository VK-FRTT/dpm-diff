package fi.vm.dpm.diff.model

class CorrelationPolicyByKeyAndParentExistence(
    baselineSourceRecords: List<SourceRecord>,
    currentSourceRecords: List<SourceRecord>
) : CorrelationPolicyByKey(
    baselineSourceRecords,
    currentSourceRecords
) {
    private val baselineRecordsByParentKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        baselineSourceRecords.groupBy { record -> record.parentKey }
    }

    private val currentRecordsByParentKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        currentSourceRecords.groupBy { record -> record.parentKey }
    }

    override fun deletedRecords(): List<SourceRecord> {
        val records = super.deletedRecords()

        return keepRecordsWhichObjectKeyExists(
            records = records,
            comparisonRecords = currentRecordsByParentKey
        )
    }

    override fun addedRecords(): List<SourceRecord> {
        val records = super.addedRecords()

        return keepRecordsWhichObjectKeyExists(
            records = records,
            comparisonRecords = baselineRecordsByParentKey
        )
    }

    private fun keepRecordsWhichObjectKeyExists(
        records: List<SourceRecord>,
        comparisonRecords: Map<CorrelationKey, List<SourceRecord>>
    ): List<SourceRecord> {
        return records.filter { record ->
            comparisonRecords[record.parentKey] != null
        }
    }
}
