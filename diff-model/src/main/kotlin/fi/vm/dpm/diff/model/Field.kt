package fi.vm.dpm.diff.model

sealed class Field(
    open val fieldName: String
)

class FallbackField(
    override val fieldName: String
) : Field(fieldName)

class RowIdentityFallbackField(
    val rowIdentityFallbacks: List<FallbackField>
) : Field("Row")

class CorrelationKeyField(
    override val fieldName: String,
    val correlationKeyKind: CorrelationKeyKind,
    val correlationFallback: FallbackField?
) : Field(fieldName) {
    fun shouldOutputRowIdentityFallback(fieldValue: Any?): Boolean {
        return (correlationFallback != null) && (fieldValue == null)
    }
}

class IdentificationLabelField(
    override val fieldName: String
) : Field(fieldName) {
    fun shouldOutputRowIdentityFallback(fieldValue: Any?): Boolean {
        return (fieldValue == null) || (fieldValue.toString().isBlank())
    }
}

class ChangeKindField : Field("Change")

class AtomField(
    override val fieldName: String,
    val atomOptions: AtomOption = AtomOption.NONE
) : Field(fieldName)

class NoteField : Field("Notes")
