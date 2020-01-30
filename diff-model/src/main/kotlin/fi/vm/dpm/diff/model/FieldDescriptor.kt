package fi.vm.dpm.diff.model

data class FieldDescriptor(
    val fieldKind: FieldKind,
    val fieldName: String,
    val fallbackCorrelationKey: FieldDescriptor? = null,
    val fallbackCorrelationNote: List<FieldDescriptor> = emptyList()
)
