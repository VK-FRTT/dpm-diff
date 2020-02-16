package fi.vm.dpm.diff.model

data class ChangeRecord(
    val fields: Map<FieldDescriptor, Any?>
) {
    companion object {

        fun resolveChanges(
            sectionDescriptor: SectionDescriptor,
            baselineBundle: SourceBundle,
            actualBundle: SourceBundle
        ): List<ChangeRecord> {

            fun deleted() = baselineBundle
                .findRecordsWithoutCorrelationIn(actualBundle)
                .map { it.toDeletedChange() }

            fun added() = actualBundle
                .findRecordsWithoutCorrelationIn(baselineBundle)
                .map { it.toAddedChange() }

            fun modified() = actualBundle
                .findAndPairRecordsWithCorrelationIn(baselineBundle)
                .mapNotNull { (actualRecord, baselineRecord) ->
                    actualRecord.toModifiedChangeOrNullFromBaseline(baselineRecord)
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

            return changeRecords
        }
    }
}
