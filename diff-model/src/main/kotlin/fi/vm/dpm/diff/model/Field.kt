package fi.vm.dpm.diff.model

sealed class Field(
    open val fieldName: String
)

data class FallbackField(
    override val fieldName: String
) : Field(fieldName)

data class RowIdentityFallbackField(
    val rowIdentityFallbacks: List<FallbackField>
) : Field("Row")

data class CorrelationKeyField(
    override val fieldName: String,
    val correlationKeyKind: CorrelationKeyKind,
    val correlationFallback: FallbackField?
) : Field(fieldName) {
    fun shouldOutputRowIdentityFallback(fieldValue: Any?): Boolean {
        return (correlationFallback != null) && (fieldValue == null)
    }
}

data class IdentificationLabelField(
    override val fieldName: String
) : Field(fieldName) {
    fun shouldOutputRowIdentityFallback(fieldValue: Any?): Boolean {
        return (fieldValue == null) || (fieldValue.toString().isBlank())
    }
}

data class ChangeKindField(
    override val fieldName: String
) : Field(fieldName)

data class AtomField(
    override val fieldName: String,
    val atomOptions: AtomOption = AtomOption.NONE
) : Field(fieldName)

data class NoteField(
    override val fieldName: String
) : Field(fieldName)
