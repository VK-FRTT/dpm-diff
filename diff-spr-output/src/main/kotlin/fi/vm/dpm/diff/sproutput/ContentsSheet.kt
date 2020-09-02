package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.ChangeReport
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.xssf.streaming.SXSSFWorkbook

object ContentsSheet {

    fun renderToWorkbook(
        workbook: SXSSFWorkbook,
        cellStyles: CellStyles,
        changeReport: ChangeReport
    ) {
        val generatorInfo = with(changeReport.reportGeneratorDescriptor) {
            "$title ($revision @ $originUrl)"
        }

        val sw = SheetWriter.createToWorkbook("Contents", workbook)
        sw.trackColumnForAutoSizing(3)

        sw.addRow(cellStyles.headerStyleNormal, "Data Point Model Change Report")
        sw.addEmptyRows(1)

        sw.addRow(cellStyles.contentStyleNormal, "Created at", changeReport.createdAt)
        sw.addRow(cellStyles.contentStyleNormal, "Baseline database", changeReport.baselineDpmDbFileName)
        sw.addRow(cellStyles.contentStyleNormal, "Current database", changeReport.currentDpmDbFileName)
        sw.addRow(cellStyles.contentStyleNormal, "Generated with", generatorInfo)
        sw.addEmptyRows(3)

        sw.addRow(cellStyles.headerStyleNormal, "Sheet", "Description", "Change count")

        changeReport.sections.forEachIndexed { index, section ->

            val sheetName = SectionSheet.composeSheetName(
                index,
                section.sectionDescriptor
            )

            sw.addLinkRow(
                cellStyles.contentStyleNormal,
                cellStyles.contentStyleNormalLink,
                SheetWriter.Link(
                    linkTitle = section.sectionDescriptor.sectionTitle,
                    linkAddress = "'$sheetName'!A1",
                    linkType = HyperlinkType.DOCUMENT
                ),
                section.sectionDescriptor.sectionDescription,
                section.changes.size.toString()
            )
        }

        sw.autoSizeColumns()
    }
}
