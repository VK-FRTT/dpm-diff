package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.Field

data class ColumnDescriptor(
    val field: Field,
    val columnTitle: String,
    val headerStyle: CellStyle,
    val contentStyle: CellStyle,
    val mapChangeValueToCell: (Any) -> String?
)
