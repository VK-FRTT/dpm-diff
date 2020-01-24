package fi.vm.dpm.diff.sproutput

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Hyperlink
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFRow

class RowWriter(
    val row: SXSSFRow
) {
    private var nextCellIndex: Int = 0

    fun addCell(cellValue: String) {
        val cell = addCell()
        cell.setCellValue(cellValue)
    }

    fun addCell(
        value: String,
        style: CellStyle
    ) {
        val cell = addCell()
        cell.setCellValue(value)
        cell.setCellStyle(style)
    }

    fun addCell(
        value: String,
        link: Hyperlink,
        style: CellStyle
    ) {
        val cell = addCell()
        cell.setCellValue(value)
        cell.setHyperlink(link)
        cell.setCellStyle(style)
    }

    private fun addCell(): SXSSFCell {
        val cell = row.createCell(nextCellIndex)
        nextCellIndex++
        return cell
    }
}
