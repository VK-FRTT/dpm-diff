package fi.vm.dpm.diff.sproutput

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Hyperlink
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFRow

class RowWriter(
    private val row: SXSSFRow
) {
    private var nextCellIndex: Int = 0

    fun addCell(
        value: String?,
        style: CellStyle
    ): SXSSFCell {
        val cell = addCell()
        cell.setCellValue(value)
        cell.cellStyle = style

        return cell
    }

    fun addLinkCell(
        value: String?,
        link: Hyperlink,
        style: CellStyle
    ): SXSSFCell {
        val cell = addCell()
        cell.setCellValue(value)
        cell.hyperlink = link
        cell.cellStyle = style

        return cell
    }

    private fun addCell(): SXSSFCell {
        val cell = row.createCell(nextCellIndex)
        nextCellIndex++

        return cell
    }
}
