package fi.vm.dpm.diff.model

interface CorrelationPolicy {

    companion object {

        fun create(
            sectionOutline: SectionOutline,
            baselineSourceRecords: List<SourceRecord>,
            currentSourceRecords: List<SourceRecord>
        ): CorrelationPolicy {

            return when (sectionOutline.sectionChangeDetectionMode) {
                ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS -> {
                    CorrelationPolicyByKey(
                        baselineSourceRecords = baselineSourceRecords,
                        currentSourceRecords = currentSourceRecords
                    )
                }

                ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE -> {
                    CorrelationPolicyByKeyAndParentExistence(
                        baselineSourceRecords = baselineSourceRecords,
                        currentSourceRecords = currentSourceRecords

                    )
                }

                ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS -> {
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
