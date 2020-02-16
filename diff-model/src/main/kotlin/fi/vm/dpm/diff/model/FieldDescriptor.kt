package fi.vm.dpm.diff.model

data class FieldDescriptor(
    val fieldKind: FieldKind,
    val fieldName: String,
    val correlationKeyKind: CorrelationKeyKind = CorrelationKeyKind.UNINITIALIZED,
    val correlationFallback: FieldDescriptor? = null,
    val noteFields: List<FieldDescriptor> = emptyList()
)
