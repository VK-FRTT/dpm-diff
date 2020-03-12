package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType

class CorrelationKey private constructor(
    private val keyValue: String,
    private val correlationKeyType: CorrelationKeyType
) {
    private enum class CorrelationKeyType(val associatedKeyKinds: List<KeyKind>) {
        FULL_KEY(
            listOf(
                KeyKind.PRIMARY_SCOPE_KEY,
                KeyKind.PRIMARY_KEY,
                KeyKind.SECONDARY_KEY
            )
        ),

        PRIMARY_KEY(
            listOf(
                KeyKind.PRIMARY_KEY,
                KeyKind.PRIMARY_KEY
            )
        )
    }

    companion object {

        fun fullKey(sourceRecord: SourceRecord): CorrelationKey {
            return createCorrelationKey(sourceRecord.fields, CorrelationKeyType.FULL_KEY)
        }

        fun primaryKey(sourceRecord: SourceRecord): CorrelationKey {
            return createCorrelationKey(sourceRecord.fields, CorrelationKeyType.PRIMARY_KEY)
        }

        private fun createCorrelationKey(
            fields: Map<Field, String?>,
            correlationKeyType: CorrelationKeyType
        ): CorrelationKey {

            val keyValue = fields
                .filterFieldType<KeyField, String?>()
                .filter { (field, _) -> field.keyKind in correlationKeyType.associatedKeyKinds }
                .map { (field, value) ->
                    value ?: if (field.keyFallback != null) {
                        fields[field.keyFallback]
                    } else {
                        null
                    }
                }
                .joinToString(separator = "|")

            return CorrelationKey(
                keyValue = keyValue,
                correlationKeyType = correlationKeyType
            )
        }
    }

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

        check(correlationKeyType == other.correlationKeyType)

        return keyValue == other.keyValue
    }
}
