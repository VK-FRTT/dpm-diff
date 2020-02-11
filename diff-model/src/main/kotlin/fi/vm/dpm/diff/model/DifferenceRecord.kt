package fi.vm.dpm.diff.model

data class DifferenceRecord(
    val fields: Map<FieldDescriptor, Any?>
) {
    companion object {

        fun resolveDifferences(
            baselineRecords: Map<String, SourceRecord>,
            actualRecords: Map<String, SourceRecord>,
            includedDifferenceKinds: Array<DifferenceKind>
        ): List<DifferenceRecord> {

            fun removed() = baselineRecords
                .filterRecordsWithoutCorrelationIn(actualRecords)
                .map { it.toRemovedDifference() }

            fun added() = actualRecords
                .filterRecordsWithoutCorrelationIn(baselineRecords)
                .map { it.toAddedDifference() }

            fun changed() = actualRecords
                .filterAndMapCorrelatingRecords(baselineRecords)
                .mapNotNull { (actualRecord, baselineRecord) ->
                    actualRecord.toChangedDifferenceOrNullFromBaseline(baselineRecord)
                }

            val differencies = includedDifferenceKinds
                .sorted()
                .flatMap { differenceKind ->
                    when (differenceKind) {
                        DifferenceKind.REMOVED -> removed()
                        DifferenceKind.ADDED -> added()
                        DifferenceKind.CHANGED -> changed()
                    }
                }

            return differencies
        }

        private fun Map<String, SourceRecord>.filterRecordsWithoutCorrelationIn(
            otherRecords: Map<String, SourceRecord>
        ): List<SourceRecord> {
            return mapNotNull { (correlationKey, primaryRecord) ->
                if (otherRecords.containsKey(correlationKey)) {
                    null
                } else {
                    primaryRecord
                }
            }
        }

        private fun Map<String, SourceRecord>.filterAndMapCorrelatingRecords(
            otherRecords: Map<String, SourceRecord>
        ): List<Pair<SourceRecord, SourceRecord>> {
            return mapNotNull { (correlationKey, primaryRecord) ->
                val correlatingRecord = otherRecords[correlationKey]

                if (correlatingRecord == null) {
                    null
                } else {
                    Pair(primaryRecord, correlatingRecord)
                }
            }
        }
    }
}
