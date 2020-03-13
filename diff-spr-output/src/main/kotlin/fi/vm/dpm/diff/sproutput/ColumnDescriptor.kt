package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.Field
import org.apache.poi.ss.usermodel.CellStyle

data class ColumnDescriptor(
    val field: Field,
    val columnTitle: String,
    val columnWidth: ColumnWidth,
    val headerStyle: CellStyle,
    val contentStyle: CellStyle,
    val changeToCellValue: (Any) -> String?
)
