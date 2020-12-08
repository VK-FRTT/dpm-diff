package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType

data class CorrelationKey private constructor(
    private val keyValue: String,
    private val correlationKeyKind: CorrelationKeyKind
) {
    private enum class CorrelationKeyKind(val associatedKeyKinds: List<KeyFieldKind>) {
        FULL_KEY(
            listOf(
                KeyFieldKind.CONTEXT_PARENT_KEY,
                KeyFieldKind.PARENT_KEY,
                KeyFieldKind.PRIME_KEY
            )
        ),

        PARENT_KEY(
            listOf(
                KeyFieldKind.CONTEXT_PARENT_KEY,
                KeyFieldKind.PARENT_KEY
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

        fun parentKey(sourceRecord: SourceRecord): CorrelationKey {
            return createCorrelationKey(sourceRecord.fields, CorrelationKeyKind.PARENT_KEY)
        }

        private fun createCorrelationKey(
            fields: Map<Field, Any?>,
            correlationKeyKind: CorrelationKeyKind
        ): CorrelationKey {
            val keyFields = fields.filterFieldType<KeyField, Any?>()

            val keyValue = correlationKeyKind
                .associatedKeyKinds
                .map { kind ->
                    keyFields
                        .filter { (field, _) -> field.keyFieldKind == kind }
                        .map { (field, value) ->
                            value ?: if (field.keyFieldFallback != null) {
                                fields[field.keyFieldFallback]
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
