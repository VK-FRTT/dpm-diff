package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.ChangeReport
import org.apache.poi.xssf.streaming.SXSSFWorkbook

object ContentsSheet {

    fun renderToWorkbook(
        workbook: SXSSFWorkbook,
        cellStyles: CellStyles,
        changeReport: ChangeReport
    ) {
        val sw = SheetWriter.createToWorkbook("Contents", workbook)
        sw.trackColumnForAutoSizing(3)

        sw.addRow(cellStyles.headerStyleNormal, "Data Point Model Change Report")
        sw.addEmptyRows(1)

        sw.addRow(cellStyles.contentStyleNormal, "Created at", changeReport.createdAt)
        sw.addRow(cellStyles.contentStyleNormal, "Baseline database", changeReport.baselineDpmDbFileName)
        sw.addRow(cellStyles.contentStyleNormal, "Current database", changeReport.currentDpmDbFileName)
        sw.addEmptyRows(3)

        sw.addRow(cellStyles.headerStyleNormal, "Sheet", "Description", "Change count")

        changeReport.sections.forEachIndexed { index, section ->

            val sheetName = SectionSheet.composeSheetName(
                index,
                section.sectionDescriptor
            )

            sw.addLinkToSheetRow(
                section.sectionDescriptor.sectionTitle,
                "'$sheetName'!A1",
                cellStyles.contentStyleNormalLink,
                cellStyles.contentStyleNormal,
                section.sectionDescriptor.sectionDescription,
                section.changes.size.toString()
            )
        }

        sw.autoSizeColumns()
    }
}
