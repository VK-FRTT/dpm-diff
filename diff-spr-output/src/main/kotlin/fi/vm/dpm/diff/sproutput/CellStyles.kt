package fi.vm.dpm.diff.sproutput

import org.apache.poi.ss.usermodel.CellStyle

data class CellStyles(
    val normalStyle: CellStyle,
    val headerStyle: CellStyle,
    val linkStyle: CellStyle
)
