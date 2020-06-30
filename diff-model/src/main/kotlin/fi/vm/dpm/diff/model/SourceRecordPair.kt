package fi.vm.dpm.diff.model

data class SourceRecordPair(
    val currentRecord: SourceRecord,
    val baselineRecord: SourceRecord
)
