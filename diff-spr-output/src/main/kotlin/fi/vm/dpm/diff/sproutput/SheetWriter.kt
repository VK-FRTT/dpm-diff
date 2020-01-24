package fi.vm.dpm.diff.sproutput

import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.xssf.streaming.SXSSFSheet

class SheetWriter(
    val sheet: SXSSFSheet,
    val cellStyles: CellStyles
) {
    private var nextRowIndex: Int = 0

    fun addRow(
        vararg cellValues: String
    ) {
        val row = addRow()

        cellValues.forEach { cellValue ->
            row.addCell(cellValue)
        }
    }

    fun addTitleRow(
        vararg cellValues: String
    ) {
        val row = addRow()

        cellValues.forEach { cellValue ->
            row.addCell(
                cellValue,
                cellStyles.titleStyle
            )
        }
    }

    fun addLinkToSheetRow(
        linkTitle: String,
        linkAddress: String,
        vararg cellValues: String
    ) {
        val row = addRow()

        val link = sheet.workbook.creationHelper.createHyperlink(HyperlinkType.DOCUMENT)
        link.setAddress(linkAddress)

        row.addCell(linkTitle, link, cellStyles.linkStyle)

        cellValues.forEach { cellValue ->
            row.addCell(cellValue)
        }
    }

    fun addEmptyRows(amount: Int) {
        repeat(amount) {
            addRow()
        }
    }

    private fun addRow(): RowWriter {
        val row = sheet.createRow(nextRowIndex)
        nextRowIndex++

        return RowWriter(
            row = row
        )
    }
}
