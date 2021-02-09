package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.ChangeReport
import fi.vm.dpm.diff.model.ChangeReportKind
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

        val sw = SheetWriter.createToWorkbook("Contents", workbook, cellStyles)
        sw.trackColumnForAutoSizing(3)

        val reportTitle = when (changeReport.reportKind) {
            ChangeReportKind.DPM -> "Data Point Model Change Report"
            ChangeReportKind.VK_DATA -> "VK Data Change Report"
        }

        sw.addRow(CellStyle.HEADER_STYLE_NORMAL, reportTitle)

        sw.addEmptyRows(1)

        sw.addRow(CellStyle.CONTENT_STYLE_NORMAL, "Created at", changeReport.createdAt)
        sw.addRow(CellStyle.CONTENT_STYLE_NORMAL, "Baseline database", changeReport.baselineFileName)
        sw.addRow(CellStyle.CONTENT_STYLE_NORMAL, "Current database", changeReport.currentFileName)
        sw.addRow(CellStyle.CONTENT_STYLE_NORMAL, "Generated with", generatorInfo)
        sw.addRow(
            CellStyle.CONTENT_STYLE_NORMAL,
            "Generation options",
            changeReport.reportGenerationOptions.joinToString(separator = "\n")
        )

        sw.addEmptyRows(3)

        sw.addRow(CellStyle.HEADER_STYLE_NORMAL, "Sheet", "Description", "Change count")

        changeReport.sections.forEachIndexed { index, section ->

            val sheetName = SectionSheet.composeSheetName(
                index,
                section.sectionOutline
            )

            sw.addLinkRow(
                CellStyle.CONTENT_STYLE_NORMAL,
                CellStyle.CONTENT_STYLE_NORMAL_LINK,
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
