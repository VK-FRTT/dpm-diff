package fi.vm.dpm.diff.sproutput

import org.apache.poi.ss.usermodel.CellStyle as PoiCellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook

class CellStyles private constructor(
    private val styleMapping: Map<CellStyle, PoiCellStyle>
) {
    companion object {

        fun initToWorkbook(workbook: Workbook): CellStyles {

            val styleMapping = mapOf(
                CellStyle.HEADER_STYLE_NORMAL to run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12
                    font.bold = true

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                },

                CellStyle.CONTENT_STYLE_NORMAL to run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                },

                CellStyle.CONTENT_STYLE_NORMAL_LINK to run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12
                    font.underline = Font.U_SINGLE
                    font.color = IndexedColors.BLUE.index

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                },

                CellStyle.HEADER_STYLE_DIMMED to run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12
                    font.bold = true
                    font.color = IndexedColors.GREY_40_PERCENT.index

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                },

                CellStyle.CONTENT_STYLE_DIMMED to run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12
                    font.color = IndexedColors.GREY_40_PERCENT.index

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                }
            )

            return CellStyles(
                styleMapping = styleMapping
            )
        }
    }

    fun poiStyle(cellStyle: CellStyle): PoiCellStyle = styleMapping.getValue(cellStyle)
}
