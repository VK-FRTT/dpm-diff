package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType

data class CorrelationKey private constructor(
    private val keyValue: String,
    private val correlationKeyKind: CorrelationKeyKind
) {
    private enum class CorrelationKeyKind(val associatedSegmentKinds: List<KeySegmentKind>) {
        FULL_KEY(
            listOf(
                KeySegmentKind.SCOPE_SEGMENT,
                KeySegmentKind.PRIME_SEGMENT,
                KeySegmentKind.SUB_SEGMENT
            )
        ),

        OBJECT_KEY(
            listOf(
                KeySegmentKind.SCOPE_SEGMENT,
                KeySegmentKind.PRIME_SEGMENT
            )
        )
    }

    companion object {

        fun fullKey(sourceRecord: SourceRecord): CorrelationKey {
            return createCorrelationKey(sourceRecord.fields, CorrelationKeyKind.FULL_KEY)
        }

        fun fullKey(fields: Map<Field, Any?>): CorrelationKey {
            return createCorrelationKey(fields, CorrelationKeyKind.FULL_KEY)
        }

        fun objectKey(sourceRecord: SourceRecord): CorrelationKey {
            return createCorrelationKey(sourceRecord.fields, CorrelationKeyKind.OBJECT_KEY)
        }

        private fun createCorrelationKey(
            fields: Map<Field, Any?>,
            correlationKeyKind: CorrelationKeyKind
        ): CorrelationKey {
            val keyFields = fields.filterFieldType<KeySegmentField, Any?>()

            val keyValue = correlationKeyKind
                .associatedSegmentKinds
                .map { segmentKind ->
                    keyFields
                        .filter { (field, _) -> field.segmentKind == segmentKind }
                        .map { (field, value) ->
                            value ?: if (field.segmentFallback != null) {
                                fields[field.segmentFallback]
                            } else {
                                null
                            }
                        }
                        .joinToString(separator = "/")
                }.joinToString(":")

            return CorrelationKey(
                keyValue = keyValue,
                correlationKeyKind = correlationKeyKind
            )
        }
    }

    fun keyValue() = keyValue

    override fun hashCode(): Int {
        return keyValue.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is CorrelationKey) {
            return false
        }

        check(correlationKeyKind == other.correlationKeyKind)

        return keyValue == other.keyValue
    }
}
