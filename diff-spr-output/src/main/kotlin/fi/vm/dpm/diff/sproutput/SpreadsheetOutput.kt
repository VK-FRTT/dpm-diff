package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.sproutput.CellStyles
import fi.vm.dpm.diff.sproutput.SheetWriter
import java.io.Closeable
import java.io.FileOutputStream
import java.nio.file.Path
import org.apache.poi.ss.usermodel.Font.U_SINGLE
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.streaming.SXSSFWorkbook

class SpreadsheetOutput(
    private val outputFilePath: Path,
    private val diagnostic: Diagnostic
) : Closeable {

    private val workbook = SXSSFWorkbook()
    private val cellStyles = initCellStyles()

    override fun close() {
        workbook.dispose()
    }

    fun renderOutput(diffReport: DiffReport) {
        addContentsSheet(diffReport)
        addSectionSheets(diffReport)

        val out = FileOutputStream(outputFilePath.toFile())
        workbook.write(out)
        out.close()

        diagnostic.info("Wrote: $outputFilePath")
    }

    private fun initCellStyles(): CellStyles {

        return CellStyles(
            titleStyle = run {
                val font = workbook.createFont()
                font.bold = true

                val style = workbook.createCellStyle()
                style.setFont(font)

                style
            },

            linkStyle = run {
                val font = workbook.createFont()
                font.underline = U_SINGLE
                font.color = IndexedColors.BLUE.index

                val style = workbook.createCellStyle()
                style.setFont(font)

                style
            }
        )
    }

    private fun addContentsSheet(diffReport: DiffReport) {
        val sw = addSheet("Contents")
        sw.sheet.trackAllColumnsForAutoSizing() // TODO

        sw.addTitleRow("Data Point Model Difference Report")

        sw.addEmptyRows(1)

        sw.addRow("Created at", diffReport.createdAt)
        sw.addRow("Baseline database", diffReport.baselineDpmDbFileName)
        sw.addRow("Actual database", diffReport.actualDpmDbFileName)

        sw.addEmptyRows(3)

        sw.addTitleRow("Sheet", "Description", "Difference count")

        diffReport.sections.forEachIndexed { index, section ->
            val sectionDescriptor = section.sectionDescriptor

            val sheetName = composeSheetName(
                sectionDescriptor,
                index
            )

            sw.addLinkToSheetRow(
                sectionDescriptor.sectionTitle,
                "'$sheetName'!A1",
                sectionDescriptor.sectionDescription,
                "TODO"
            )
        }

        sw.sheet.autoSizeColumn(0)
        sw.sheet.autoSizeColumn(1)
        sw.sheet.autoSizeColumn(2)
    }

    private fun addSectionSheets(diffReport: DiffReport) {
        diffReport.sections.forEachIndexed { index, section ->
            val sheetName = composeSheetName(
                section.sectionDescriptor,
                index
            )

            val sw = addSheet(sheetName)
            sw.addRow("TODO")
        }
    }

    private fun addSheet(sheetName: String): SheetWriter {
        val sheet = workbook.createSheet()
        val index = workbook.getSheetIndex(sheet)
        workbook.setSheetName(index, sheetName)

        return SheetWriter(
            sheet = sheet,
            cellStyles = cellStyles
        )
    }

    private fun composeSheetName(
        sectionDescriptor: SectionDescriptor,
        sectionIndex: Int
    ): String {
        return "${String.format("%02d", sectionIndex + 1)}_${sectionDescriptor.sectionShortTitle}".replace(" ", "_")
    }
}
