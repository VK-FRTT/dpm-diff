package fi.vm.dpm.diff.sproutput

import ext.kotlin.splitCamelCaseWords
import fi.vm.dpm.diff.model.ReportSection
import fi.vm.dpm.diff.model.SectionOutline
import org.apache.poi.xssf.streaming.SXSSFWorkbook

object SectionSheet {

    fun renderToWorkbook(
        workbook: SXSSFWorkbook,
        cellStyles: CellStyles,
        section: ReportSection,
        sectionIndex: Int
    ) {
        val sheetName = composeSheetName(
            sectionIndex,
            section.sectionOutline
        )

        val sw = SheetWriter.createToWorkbook(sheetName, workbook, cellStyles)

        val columns = SectionSheetColumns.mapFieldsToColumns(
            section.sectionOutline.sectionFields
        )

        val headerCells = columns.map {
            Triple(
                first = it.columnTitle.splitCamelCaseWords().toUpperCase(),
                second = it.headerStyle,
                third = it.field.displayHint
            )
        }
        sw.addHeaderRow(headerCells)

        section.changes.forEach { changeRecord ->
            val contentCells = columns
                .map { column ->
                    val changeValue = changeRecord.fields[column.field]

                    Pair(
                        first = changeValue?.let { column.mapChangeValueToCell(it) },
                        second = column.contentStyle
                    )
                }
            sw.addRow(contentCells)
        }
    }

    fun composeSheetName(
        sectionIndex: Int,
        sectionOutline: SectionOutline
    ): String {
        return "${String.format("%02d", sectionIndex + 1)}_${sectionOutline.sectionShortTitle.splitCamelCaseWords("_")}"
    }
}
