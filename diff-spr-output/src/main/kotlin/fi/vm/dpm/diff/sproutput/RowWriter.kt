package fi.vm.dpm.diff.sproutput

import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFRow

class RowWriter(
    private val row: SXSSFRow,
    private val cellStyles: CellStyles
) {
    private var nextCellIndex: Int = 0

    fun addCell(
        value: String?,
        style: CellStyle
    ): SXSSFCell {
        val cell = addCell()
        cell.setCellValue(value)
        cell.cellStyle = cellStyles.poiStyle(style)

        return cell
    }

    fun addLinkCell(
        value: String,
        style: CellStyle,
        linkAddress: String,
        linkType: HyperlinkType,
        creationHelper: CreationHelper
    ): SXSSFCell {
        val link = creationHelper.createHyperlink(linkType)
        link.address = linkAddress

        val cell = addCell()
        cell.setCellValue(value)
        cell.hyperlink = link
        cell.cellStyle = cellStyles.poiStyle(style)

        return cell
    }

    private fun addCell(): SXSSFCell {
        val cell = row.createCell(nextCellIndex)
        nextCellIndex++

        return cell
    }
}
