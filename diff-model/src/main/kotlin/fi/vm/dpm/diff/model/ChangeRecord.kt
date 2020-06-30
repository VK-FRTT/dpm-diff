package fi.vm.dpm.diff.model

data class ChangeRecord(
    val fields: Map<Field, Any?>
) {
    companion object {

        fun resolveChanges(
            sectionDescriptor: SectionDescriptor,
            baselineSourceRecords: List<SourceRecord>,
            currentSourceRecords: List<SourceRecord>
        ): List<ChangeRecord> {

            val correlationPolicy = CorrelationPolicy.create(
                sectionDescriptor = sectionDescriptor,
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
                .sameIdentityRecordPairs()
                .mapNotNull { recordPair ->
                    recordPair.currentRecord.toModifiedChangeOrNullFromBaseline(
                        recordPair.baselineRecord
                    )
                }

            val changeRecords = sectionDescriptor.includedChanges
                .sorted()
                .flatMap { changeKind ->
                    when (changeKind) {
                        ChangeKind.DELETED -> deleted()
                        ChangeKind.ADDED -> added()
                        ChangeKind.MODIFIED -> modified()
                    }
                }

            val comparator = ChangeRecordComparator(sectionDescriptor.sectionSortOrder)
            return changeRecords.sortedWith(comparator)
        }
    }
}
