package fi.vm.dpm.diff.model

class CorrelationPolicySubObject(
    baselineSourceRecords: List<SourceRecord>,
    currentSourceRecords: List<SourceRecord>
) : CorrelationPolicy(
    baselineSourceRecords,
    currentSourceRecords
) {
    private val baselineRecordsByObjectKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        baselineSourceRecords.groupBy { record -> record.objectKey }
    }

    private val currentRecordsByObjectKey: Map<CorrelationKey, List<SourceRecord>> by lazy {
        currentSourceRecords.groupBy { record -> record.objectKey }
    }

    override fun deletedRecords(): List<SourceRecord> {
        val records = super.deletedRecords()

        return keepRecordsWhichObjectKeyExists(
            records = records,
            comparisonRecords = currentRecordsByObjectKey
        )
    }

    override fun addedRecords(): List<SourceRecord> {
        val records = super.addedRecords()

        return keepRecordsWhichObjectKeyExists(
            records = records,
            comparisonRecords = baselineRecordsByObjectKey
        )
    }

    private fun keepRecordsWhichObjectKeyExists(
        records: List<SourceRecord>,
        comparisonRecords: Map<CorrelationKey, List<SourceRecord>>
    ): List<SourceRecord> {
        return records.filter { record ->
            comparisonRecords[record.objectKey] != null
        }
    }
}
