package fi.vm.dpm.diff.model

interface CorrelationPolicy {

    companion object {

        fun create(
            sectionOutline: SectionOutline,
            baselineSourceRecords: List<SourceRecord>,
            currentSourceRecords: List<SourceRecord>
        ): CorrelationPolicy {

            return when (sectionOutline.sectionCorrelationMode) {
                CorrelationMode.CORRELATION_BY_KEY -> {
                    CorrelationPolicyByKey(
                        baselineSourceRecords = baselineSourceRecords,
                        currentSourceRecords = currentSourceRecords
                    )
                }

                CorrelationMode.CORRELATION_BY_KEY_AND_PARENT_EXISTENCE -> {
                    CorrelationPolicyByKeyAndParentExistence(
                        baselineSourceRecords = baselineSourceRecords,
                        currentSourceRecords = currentSourceRecords

                    )
                }

                CorrelationMode.CORRELATION_BY_KEYS_AND_ATOMS_VALUES -> {
                    CorrelationPolicyByKeyAndAtomValues(
                        baselineSourceRecords = baselineSourceRecords,
                        currentSourceRecords = currentSourceRecords
                    )
                }
            }
        }
    }

    fun deletedRecords(): List<SourceRecord>
    fun addedRecords(): List<SourceRecord>
    fun correlatingRecordPairs(): List<SourceRecordPair>
    fun duplicateCorrelationKeyRecords(): List<SourceRecord>
}
