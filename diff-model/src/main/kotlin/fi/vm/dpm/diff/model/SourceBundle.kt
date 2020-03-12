package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType

class SourceBundle(
    private val sectionDescriptor: SectionDescriptor,
    private val sourceRecords: List<SourceRecord>
) {
    private enum class CorrelationMode {
        SINGLE_PHASE_BY_FULL_KEY,
        TWO_PHASE_BY_PRIMARY_AND_FULL_KEY,
    }

    private val correlationMode: CorrelationMode by lazy {
        if (sectionDescriptor.sectionFields
                .filterFieldType<KeyField>()
                .any { it.keyKind == KeyKind.SECONDARY_KEY }
        ) {
            CorrelationMode.TWO_PHASE_BY_PRIMARY_AND_FULL_KEY
        } else {
            CorrelationMode.SINGLE_PHASE_BY_FULL_KEY
        }
    }

    private val allCandidates: Map<CorrelationKey, SourceRecord> by lazy {
        sourceRecords
            .groupBy { record -> record.fullKey }
            .map { (fullKey, records) ->
                val record = when (records.size) {
                    0 -> thisShouldNeverHappen("No correlation candidate for key: $fullKey")
                    1 -> records[0]
                    else -> thisShouldNeverHappen("More than one correlation candidate for key: $fullKey")
                }

                fullKey to record
            }.toMap()
    }

    private val candidatesByPrimaryKey: Map<CorrelationKey, Map<CorrelationKey, SourceRecord>> by lazy {
        sourceRecords
            .groupBy { record -> record.primaryKey }
            .map { (primaryKey, records) ->
                val candidates = records.map { record -> record.fullKey to record }.toMap()
                primaryKey to candidates
            }.toMap()
    }

    fun findRecordsWithoutCorrelationIn(otherBundle: SourceBundle): List<SourceRecord> {
        return sourceRecords.mapNotNull { pivotRecord ->
            val correlationCandidates = otherBundle.correlationCandidatesForPivotRecord(pivotRecord)
            correlationCandidates ?: return@mapNotNull null

            if (correlationCandidates.containsKey(pivotRecord.fullKey)) {
                null
            } else {
                pivotRecord
            }
        }
    }

    fun findAndPairRecordsWithCorrelationIn(otherBundle: SourceBundle): List<Pair<SourceRecord, SourceRecord>> {
        return sourceRecords.mapNotNull { pivotRecord ->

            val correlationCandidates = otherBundle.correlationCandidatesForPivotRecord(pivotRecord)
            correlationCandidates ?: return@mapNotNull null

            val correlatingRecord = correlationCandidates[pivotRecord.fullKey]

            if (correlatingRecord == null) {
                null
            } else {
                Pair(pivotRecord, correlatingRecord)
            }
        }
    }

    private fun correlationCandidatesForPivotRecord(pivotRecord: SourceRecord): Map<CorrelationKey, SourceRecord>? {
        return when (correlationMode) {
            CorrelationMode.SINGLE_PHASE_BY_FULL_KEY -> allCandidates
            CorrelationMode.TWO_PHASE_BY_PRIMARY_AND_FULL_KEY -> candidatesByPrimaryKey[pivotRecord.primaryKey]
        }
    }
}
