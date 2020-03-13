package fi.vm.dpm.diff.model

sealed class Field(
    open val fieldName: String
)

class FallbackField(
    override val fieldName: String
) : Field(fieldName)

class RecordIdentityFallbackField(
    val identityFallbacks: List<FallbackField>
) : Field("Row")

class KeyField(
    override val fieldName: String,
    val keyKind: KeyKind,
    val keyFallback: FallbackField?
) : Field(fieldName) {
    fun shouldOutputRecordIdentityFallback(fieldValue: Any?): Boolean {
        return (keyFallback != null) && (fieldValue == null)
    }
}

class IdentificationLabelField(
    override val fieldName: String
) : Field(fieldName) {
    fun shouldOutputRecordIdentityFallback(fieldValue: Any?): Boolean {
        return (fieldValue == null) || (fieldValue.toString().isBlank())
    }
}

class ChangeKindField : Field("Change")

class AtomField(
    override val fieldName: String,
    val atomOptions: AtomOption = AtomOption.NONE
) : Field(fieldName)

class NoteField : Field("Notes")