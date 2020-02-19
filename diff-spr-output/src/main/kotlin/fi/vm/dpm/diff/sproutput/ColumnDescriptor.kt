package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.FieldDescriptor

data class ColumnDescriptor(
    val field: FieldDescriptor,
    val columnTitle: String,
    val toColumnValue: (Any) -> String?
)
