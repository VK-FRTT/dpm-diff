package fi.vm.dpm.diff.model

data class ChangeRecord(
    val fields: Map<Field, Any?>
) {
    companion object {

        fun resolveChanges(
            sectionOutline: SectionOutline,
            baselineSourceRecords: List<SourceRecord>,
            currentSourceRecords: List<SourceRecord>
        ): List<ChangeRecord> {

            val correlationPolicy = CorrelationPolicy.create(
                sectionOutline = sectionOutline,
                baselineSourceRecords = baselineSourceRecords,
                currentSourceRecords = currentSourceRecords
            )

            fun deleted() = correlationPolicy
                .deletedRecords()
                .map { it.toDeletedChange() }

            fun added() = correlationPolicy
                .addedRecords()
                .map { it.toAddedChange() }

            fun modified() = correlationPolicy
                .correlatingRecordPairs()
                .mapNotNull { recordPair ->
                    recordPair.currentRecord.toModifiedChangeOrNullFromBaseline(
                        recordPair.baselineRecord
                    )
                }

            fun duplicateKey() = correlationPolicy
                .duplicateCorrelationKeyRecords()
                .map { it.toDuplicateKeyChange() }

            return sectionOutline.includedChanges
                .sorted()
                .flatMap { changeKind ->
                    when (changeKind) {
                        ChangeKind.DELETED -> deleted()
                        ChangeKind.ADDED -> added()
                        ChangeKind.MODIFIED -> modified()
                        ChangeKind.DUPLICATE_KEY -> duplicateKey()
                    }
                }
        }
    }
}
