package fi.vm.dpm.diff.model

data class FieldDescriptor(
    val fieldKind: FieldKind,
    val fieldName: String,
    val correlationKeyFallback: FieldDescriptor? = null,
    val noteFallback: List<FieldDescriptor> = emptyList()
)
