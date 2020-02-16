package fi.vm.dpm.diff.model

data class CorrelationKey(
    private val key: String,
    private val type: Type
) {
    enum class Type(val relatedKeyKinds: List<CorrelationKeyKind>) {
        FULL_KEY(
            listOf(
                CorrelationKeyKind.PRIMARY_KEY,
                CorrelationKeyKind.SECONDARY_KEY
            )
        ),

        PRIMARY_KEY(
            listOf(
                CorrelationKeyKind.PRIMARY_KEY
            )
        )
    }

    companion object {

        fun fullKey(sourceRecord: SourceRecord): CorrelationKey {
            return composeKey(sourceRecord.fields, Type.FULL_KEY)
        }

        fun primaryKey(sourceRecord: SourceRecord): CorrelationKey {
            return composeKey(sourceRecord.fields, Type.PRIMARY_KEY)
        }

        private fun composeKey(
            fields: Map<FieldDescriptor, String?>,
            type: Type
        ): CorrelationKey {

            val key = fields
                .filter { (field, _) ->
                    (field.fieldKind == FieldKind.CORRELATION_KEY) && (field.correlationKeyKind in type.relatedKeyKinds)
                }
                .map { (field, value) ->
                    if (value == null && field.correlationFallback != null) {
                        fields[field.correlationFallback]
                    } else {
                        value
                    }
                }
                .joinToString(separator = "|")

            return CorrelationKey(key, type)
        }
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is CorrelationKey) {
            return false
        }

        check(type == other.type)

        return key == other.key
    }
}
