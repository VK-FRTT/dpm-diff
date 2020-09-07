package fi.vm.dpm.diff.model

sealed class Field(
    open val fieldName: String,
    open val displayHint: DisplayHint
)

class FallbackField(
    override val fieldName: String
) : Field(fieldName, DisplayHint.NO_DISPLAY)

class RecordIdentityFallbackField(
    val identityFallbacks: List<FallbackField>
) : Field("Row", DisplayHint.NO_DISPLAY)

class KeySegmentField(
    override val fieldName: String,
    val segmentKind: KeySegmentKind,
    val segmentFallback: FallbackField?
) : Field(fieldName, DisplayHint.FIT_BY_TITLE) {
    fun shouldOutputRecordIdentityFallback(fieldValue: Any?): Boolean {
        return (segmentFallback != null) && (fieldValue == null)
    }
}

class IdentificationLabelField(
    override val fieldName: String
) : Field(fieldName, DisplayHint.FIT_BY_TITLE) {
    fun shouldOutputRecordIdentityFallback(fieldValue: Any?): Boolean {
        return (fieldValue == null) || (fieldValue.toString().isBlank())
    }
}

class ChangeKindField : Field("Change", DisplayHint.FIT_BY_TITLE)

class AtomField(
    override val fieldName: String,
    override val displayHint: DisplayHint = DisplayHint.FIT_BY_TITLE,
    val atomOptions: AtomOption = AtomOption.NONE
) : Field(fieldName, displayHint)

class NoteField : Field("Notes", DisplayHint.FIXED_WIDE)
