package fi.vm.dpm.diff.model

data class DifferenceRecord(
    val fields: Map<FieldDescriptor, Any?>
) {
    companion object {
        fun resolveDifferences(
            baselineRecords: Map<String, SourceRecord>,
            actualRecords: Map<String, SourceRecord>
        ): List<DifferenceRecord> {

            val removed = baselineRecords
                .filterRecordsWithoutCorrelationIn(actualRecords)
                .map { it.toRemovedDifference() }

            val added = actualRecords
                .filterRecordsWithoutCorrelationIn(baselineRecords)
                .map { it.toAddedDifference() }

            val changed = actualRecords
                .filterAndMapCorrelatingRecords(baselineRecords)
                .mapNotNull { (actualRecord, baselineRecord) ->
                    actualRecord.toChangedDifferenceOrNullFromBaseline(baselineRecord)
                }

            return removed + added + changed
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
