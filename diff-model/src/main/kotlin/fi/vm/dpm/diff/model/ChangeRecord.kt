package fi.vm.dpm.diff.model

data class ChangeRecord(
    val fields: Map<Field, Any?>
) {
    companion object {

        fun resolveChanges(
            sectionDescriptor: SectionDescriptor,
            baselineBundle: SourceBundle,
            currentBundle: SourceBundle
        ): List<ChangeRecord> {

            fun deleted() = baselineBundle
                .findRecordsWithoutCorrelationIn(currentBundle)
                .map { it.toDeletedChange() }

            fun added() = currentBundle
                .findRecordsWithoutCorrelationIn(baselineBundle)
                .map { it.toAddedChange() }

            fun modified() = currentBundle
                .findAndPairRecordsWithCorrelationIn(baselineBundle)
                .mapNotNull { (currentRecord, baselineRecord) ->
                    currentRecord.toModifiedChangeOrNullFromBaseline(baselineRecord)
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
