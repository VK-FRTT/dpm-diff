package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.sproutput.CellStyles
import fi.vm.dpm.diff.sproutput.ColumnDescriptor
import fi.vm.dpm.diff.sproutput.ColumnKind
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

    fun renderOutput(diffReport: DifferenceReport) {
        with(diagnostic) {
            info("Writing spreadsheet...")
        }

        addContentsSheet(diffReport)
        addSectionSheets(diffReport)

        val out = FileOutputStream(outputFilePath.toFile())
        workbook.write(out)
        out.close()

        with(diagnostic) {
            info("Wrote: $outputFilePath")
        }
    }

    private fun initCellStyles(): CellStyles {

        return CellStyles(
            headerStyle = run {
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

    private fun addContentsSheet(diffReport: DifferenceReport) {
        val sw = addSheet("Contents")
        sw.sheet.trackAllColumnsForAutoSizing() // TODO

        sw.addHeaderRow("Data Point Model Difference Report")

        sw.addEmptyRows(1)

        sw.addRow("Created at", diffReport.createdAt)
        sw.addRow("Baseline database", diffReport.baselineDpmDbFileName)
        sw.addRow("Actual database", diffReport.actualDpmDbFileName)

        sw.addEmptyRows(3)

        sw.addHeaderRow("Sheet", "Description", "Difference count")

        diffReport.sections.forEachIndexed { index, section ->

            val sheetName = composeSheetName(
                section.sectionDescriptor,
                index
            )

            sw.addLinkToSheetRow(
                section.sectionDescriptor.sectionTitle,
                "'$sheetName'!A1",
                section.sectionDescriptor.sectionDescription,
                section.differences.size.toString()
            )
        }

        sw.sheet.autoSizeColumn(0)
        sw.sheet.autoSizeColumn(1)
        sw.sheet.autoSizeColumn(2)
    }

    private fun addSectionSheets(diffReport: DifferenceReport) {
        diffReport.sections.forEachIndexed { index, section ->
            val sheetName = composeSheetName(
                section.sectionDescriptor,
                index
            )

            val sw = addSheet(sheetName)

            val columns = composeSectionColumns(section)
            addSectionTitleRow(columns, sw)
            addSectionValueRows(columns, section, sw)
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

    private fun composeSectionColumns(
        reportSection: ReportSection
    ): List<ColumnDescriptor> {

        return reportSection.sectionDescriptor.sectionFields.flatMap { field ->

            when (field.fieldKind) {
                FieldKind.CORRELATION_ID -> listOf(
                    ColumnDescriptor(
                        ColumnKind.CORRELATION_ID,
                        field
                    )
                )

                FieldKind.DISCRIMINATION_LABEL -> listOf(
                    ColumnDescriptor(
                        ColumnKind.CORRELATION_ID,
                        field
                    )
                )

                FieldKind.DIFFERENCE_KIND -> listOf(
                    ColumnDescriptor(
                        ColumnKind.CORRELATION_ID,
                        field
                    )
                )

                FieldKind.CHANGE -> listOf(
                    ColumnDescriptor(
                        ColumnKind.CHANGE_ACTUAL,
                        field
                    ),

                    ColumnDescriptor(
                        ColumnKind.CHANGE_BASELINE,
                        field
                    )
                )
            }
        }
    }

    private fun addSectionTitleRow(
        columns: List<ColumnDescriptor>,
        sheetWriter: SheetWriter
    ) {
        val titles = columns
            .map { it.title() }
            .toTypedArray()

        sheetWriter.addHeaderRow(*titles)
    }

    private fun addSectionValueRows(
        columns: List<ColumnDescriptor>,
        reportSection: ReportSection,
        sheetWriter: SheetWriter
    ) {
        reportSection.differences.forEach { difference ->

            val columnValues = columns.map { column ->

                val differenceValue = difference.fields[column.field]

                if (differenceValue == null) {
                    null
                } else {
                    when (column.columnKind) {
                        ColumnKind.CORRELATION_ID -> differenceValue.toString()
                        ColumnKind.DISCRIMINATION_LABEL -> differenceValue.toString()
                        ColumnKind.DIFFERENCE_KIND -> differenceValue.toString()

                        ColumnKind.CHANGE_ACTUAL -> {
                            val change = differenceValue as ChangeValue
                            change.actualValue
                        }
                        ColumnKind.CHANGE_BASELINE -> {
                            val change = differenceValue as ChangeValue
                            change.baselineValue
                        }
                    }
                }
            }

            sheetWriter.addRow(*columnValues.toTypedArray())
        }
    }

    private fun composeSheetName(
        sectionDescriptor: SectionDescriptor,
        sectionIndex: Int
    ): String {
        return "${String.format("%02d", sectionIndex + 1)}_${sectionDescriptor.sectionShortTitle}".replace(" ", "_")
    }
}
