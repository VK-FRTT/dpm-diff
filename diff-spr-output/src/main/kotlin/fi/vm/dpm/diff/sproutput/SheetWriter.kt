package fi.vm.dpm.diff.sproutput

import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.SheetUtil
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook

class SheetWriter(
    private val sheet: SXSSFSheet
) {
    companion object {
        fun createToWorkbook(sheetName: String, workbook: SXSSFWorkbook): SheetWriter {
            val sheet = workbook.createSheet()
            val index = workbook.getSheetIndex(sheet)
            workbook.setSheetName(index, sheetName)

            return SheetWriter(
                sheet = sheet
            )
        }
    }

    private var nextRowIndex: Int = 0
    private var autoSizingColumns: Int = 0

    fun addHeaderRow(
        cellsData: List<Triple<String?, CellStyle, ColumnWidth>>
    ) {
        val formatter = DataFormatter()
        val defaultCharWidth = SheetUtil.getDefaultCharWidth(sheet.workbook)

        val row = addRow()

        cellsData.forEachIndexed { index, cellData ->
            val cell = row.addCell(cellData.first, cellData.second)

            val cellWidth = if (cellData.third == ColumnWidth.FIT_TITLE_CONTENT_WITH_MARGIN) {
                SheetUtil.getCellWidth(cell, defaultCharWidth, formatter, false)
                    .let { it * 256 + ColumnWidth.FIT_TITLE_CONTENT_WITH_MARGIN.width }
                    .toInt()
                    .coerceIn(0..256 * 256)
            } else {
                cellData.third.width
            }

            sheet.setColumnWidth(index, cellWidth)
        }

        sheet.setAutoFilter(
            CellRangeAddress(0, 0, 0, cellsData.size - 1)
        )

        sheet.createFreezePane(0, 1)
    }

    fun addRow(
        cellsData: List<Pair<String?, CellStyle>>
    ) {
        val row = addRow()

        cellsData.forEach { cellData ->
            row.addCell(cellData.first, cellData.second)
        }
    }

    fun addRow(
        cellStyle: CellStyle,
        vararg cellValues: String?
    ) {
        val row = addRow()

        cellValues.forEach { cellValue ->
            row.addCell(cellValue, cellStyle)
        }
    }

    fun addLinkToSheetRow(
        linkTitle: String,
        linkAddress: String,
        linkStyle: CellStyle,
        cellStyle: CellStyle,
        vararg cellValues: String?
    ) {
        val row = addRow()

        val link = sheet.workbook.creationHelper.createHyperlink(HyperlinkType.DOCUMENT)
        link.address = linkAddress

        row.addLinkCell(linkTitle, link, linkStyle)

        cellValues.forEach { cellValue ->
            row.addCell(cellValue, cellStyle)
        }
    }

    fun addEmptyRows(amount: Int) {
        repeat(amount) {
            addRow()
        }
    }

    fun trackColumnForAutoSizing(columnsCount: Int) {
        autoSizingColumns = columnsCount
        repeat(autoSizingColumns) { sheet.trackColumnForAutoSizing(it) }
    }

    fun autoSizeColumns() {
        repeat(autoSizingColumns) { sheet.autoSizeColumn(it) }
    }

    private fun addRow(): RowWriter {
        val row = sheet.createRow(nextRowIndex)
        nextRowIndex++

        return RowWriter(
            row = row
        )
    }
}
