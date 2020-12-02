package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.ChangeReport
import fi.vm.dpm.diff.model.ChangeReportKind
import fi.vm.dpm.diff.model.thisShouldNeverHappen
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

        val reportTitle = when (changeReport.reportKind) {
            ChangeReportKind.DPM -> "Data Point Model Change Report"
            ChangeReportKind.VK_DATA -> "VK Data Change Report"
            else -> thisShouldNeverHappen("Unsupported report kind")
        }

        sw.addRow(cellStyles.headerStyleNormal, reportTitle)

        sw.addEmptyRows(1)

        sw.addRow(cellStyles.contentStyleNormal, "Created at", changeReport.createdAt)
        sw.addRow(cellStyles.contentStyleNormal, "Baseline database", changeReport.baselineFileName)
        sw.addRow(cellStyles.contentStyleNormal, "Current database", changeReport.currentFileName)
        sw.addRow(cellStyles.contentStyleNormal, "Generated with", generatorInfo)
        sw.addRow(
            cellStyles.contentStyleNormal,
            "Generation options",
            changeReport.reportGenerationOptions.joinToString(separator = "\n")
        )

        sw.addEmptyRows(3)

        sw.addRow(cellStyles.headerStyleNormal, "Sheet", "Description", "Change count")

        changeReport.sections.forEachIndexed { index, section ->

            val sheetName = SectionSheet.composeSheetName(
                index,
                section.sectionOutline
            )

            sw.addLinkRow(
                cellStyles.contentStyleNormal,
                cellStyles.contentStyleNormalLink,
                SheetWriter.Link(
                    linkTitle = section.sectionOutline.sectionTitle,
                    linkAddress = "'$sheetName'!A1",
                    linkType = HyperlinkType.DOCUMENT
                ),
                section.sectionOutline.sectionDescription,
                section.changes.size.toString()
            )
        }

        sw.autoSizeColumns()
    }
}
