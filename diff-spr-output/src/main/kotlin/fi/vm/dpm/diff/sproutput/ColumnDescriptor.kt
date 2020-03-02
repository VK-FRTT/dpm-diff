package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.Field

data class ColumnDescriptor(
    val field: Field,
    val columnTitle: String,
    val columnWidth: ColumnWidth,
    val toColumnValue: (Any) -> String?
)
