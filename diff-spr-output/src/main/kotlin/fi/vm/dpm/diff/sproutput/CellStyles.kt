package fi.vm.dpm.diff.sproutput

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook

class CellStyles(
    val headerStyleNormal: CellStyle,
    val contentStyleNormal: CellStyle,
    val contentStyleNormalLink: CellStyle,
    val headerStyleDimmed: CellStyle,
    val contentStyleDimmed: CellStyle
) {
    companion object {
        fun initCellStylesToWorkbook(workbook: Workbook): CellStyles {

            return CellStyles(
                headerStyleNormal = run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12
                    font.bold = true

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                },

                contentStyleNormal = run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                },

                contentStyleNormalLink = run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12
                    font.underline = Font.U_SINGLE
                    font.color = IndexedColors.BLUE.index

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                },

                headerStyleDimmed = run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12
                    font.bold = true
                    font.color = IndexedColors.GREY_40_PERCENT.index

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                },

                contentStyleDimmed = run {
                    val font = workbook.createFont()
                    font.fontHeightInPoints = 12
                    font.color = IndexedColors.GREY_40_PERCENT.index

                    val style = workbook.createCellStyle()
                    style.setFont(font)
                    style.wrapText = true
                    style
                }
            )
        }
    }
}
