package fi.vm.dpm.diff.model

sealed class Field(
    open val fieldName: String
)

data class FallbackField(
    override val fieldName: String
) : Field(fieldName)

data class CorrelationKeyField(
    override val fieldName: String,
    val correlationKeyKind: CorrelationKeyKind,
    val correlationFallback: FallbackField?,
    val noteFallbacks: List<FallbackField>
) : Field(fieldName)

data class IdentificationLabelField(
    override val fieldName: String,
    val noteFallbacks: List<FallbackField>
) : Field(fieldName)

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
