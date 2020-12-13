package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType

data class CorrelationKey private constructor(
    private val keyValue: String,
    private val correlationKeyKind: CorrelationKeyKind
) {
    companion object {

        fun createCorrelationKey(
            correlationKeyKind: CorrelationKeyKind,
            sourceRecord: SourceRecord
        ): CorrelationKey {
            return createCorrelationKey(
                correlationKeyKind,
                sourceRecord.fields
            )
        }

        fun createCorrelationKey(
            correlationKeyKind: CorrelationKeyKind,
            fields: Map<Field, Any?>
        ): CorrelationKey {
            return when (correlationKeyKind) {
                CorrelationKeyKind.FULL_KEY_FIELD_CORRELATION_KEY -> createKeyFieldCorrelationKey(
                    fields,
                    fullKeyFieldKinds,
                    correlationKeyKind
                )
                CorrelationKeyKind.PARENT_KEY_FIELD_CORRELATION_KEY -> createKeyFieldCorrelationKey(
                    fields,
                    parentKeyFieldKinds,
                    correlationKeyKind
                )
                CorrelationKeyKind.ATOM_FIELD_CORRELATION_KEY -> createAtomFieldCorrelationKey(fields)
            }
        }

        private fun createKeyFieldCorrelationKey(
            fields: Map<Field, Any?>,
            includedKeyFieldKinds: List<KeyFieldKind>,
            correlationKeyKind: CorrelationKeyKind
        ): CorrelationKey {
            val keyFields = fields.filterFieldType<KeyField, Any?>()

            val keyValue = includedKeyFieldKinds
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

        private fun createAtomFieldCorrelationKey(
            fields: Map<Field, Any?>
        ): CorrelationKey {
            val atomFields = fields.filterFieldType<AtomField, Any?>()

            val keyValue = atomFields.values.joinToString(separator = "/")

            return CorrelationKey(
                keyValue = keyValue,
                correlationKeyKind = CorrelationKeyKind.ATOM_FIELD_CORRELATION_KEY
            )
        }

        private val fullKeyFieldKinds = listOf(
            KeyFieldKind.CONTEXT_PARENT_KEY,
            KeyFieldKind.PARENT_KEY,
            KeyFieldKind.PRIME_KEY
        )

        private val parentKeyFieldKinds = listOf(
            KeyFieldKind.CONTEXT_PARENT_KEY,
            KeyFieldKind.PARENT_KEY
        )
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
