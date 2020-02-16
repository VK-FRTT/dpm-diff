package fi.vm.dpm.diff.model

import ext.kotlin.replaceCamelCase
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.sproutput.CellStyles
import fi.vm.dpm.diff.sproutput.ColumnDescriptor
import fi.vm.dpm.diff.sproutput.FieldSpecifier
import fi.vm.dpm.diff.sproutput.SheetWriter
import java.io.Closeable
import java.io.FileOutputStream
import java.nio.file.Path
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Font.U_SINGLE
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.SheetUtil
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

    fun renderOutput(changeReport: ChangeReport) {
        with(diagnostic) {
            info("Writing spreadsheet...")
        }

        addContentsSheet(changeReport)
        addSectionSheets(changeReport)

        val out = FileOutputStream(outputFilePath.toFile())
        workbook.write(out)
        out.close()

        with(diagnostic) {
            info("Wrote: $outputFilePath")
        }
    }

    private fun initCellStyles(): CellStyles {

        return CellStyles(
            normalStyle = run {
                val font = workbook.createFont()
                font.setFontHeightInPoints(12)

                val style = workbook.createCellStyle()
                style.setFont(font)
                style.wrapText = true
                style
            },

            headerStyle = run {
                val font = workbook.createFont()
                font.setFontHeightInPoints(12)
                font.bold = true

                val style = workbook.createCellStyle()
                style.setFont(font)
                style.wrapText = true
                style
            },

            linkStyle = run {
                val font = workbook.createFont()
                font.setFontHeightInPoints(12)
                font.underline = U_SINGLE
                font.color = IndexedColors.BLUE.index

                val style = workbook.createCellStyle()
                style.setFont(font)
                style.wrapText = true
                style
            }
        )
    }

    private fun addContentsSheet(changeReport: ChangeReport) {
        val sw = addSheet("Contents")
        val contentsSheetColumnCount = 3
        repeat(contentsSheetColumnCount) { sw.sheet.trackColumnForAutoSizing(it) }

        sw.addHeaderRow("Data Point Model Change Report")

        sw.addEmptyRows(1)

        sw.addRow("Created at", changeReport.createdAt)
        sw.addRow("Baseline database", changeReport.baselineDpmDbFileName)
        sw.addRow("Actual database", changeReport.actualDpmDbFileName)

        sw.addEmptyRows(3)

        sw.addHeaderRow("Sheet", "Description", "Change count")

        changeReport.sections.forEachIndexed { index, section ->

            val sheetName = composeSheetName(
                section.sectionDescriptor,
                index
            )

            sw.addLinkToSheetRow(
                section.sectionDescriptor.sectionTitle,
                "'$sheetName'!A1",
                section.sectionDescriptor.sectionDescription,
                section.changes.size.toString()
            )
        }

        repeat(contentsSheetColumnCount) { sw.sheet.autoSizeColumn(it) }
    }

    private fun addSectionSheets(diffReport: ChangeReport) {
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

                FieldKind.CORRELATION_KEY -> listOf(
                    ColumnDescriptor(
                        field,
                        FieldSpecifier.NONE
                    )
                )

                FieldKind.FALLBACK_VALUE -> emptyList()

                FieldKind.IDENTIFICATION_LABEL -> listOf(
                    ColumnDescriptor(
                        field,
                        FieldSpecifier.NONE
                    )
                )

                FieldKind.CHANGE_KIND -> listOf(
                    ColumnDescriptor(
                        field,
                        FieldSpecifier.NONE
                    )
                )

                FieldKind.ATOM -> listOf(
                    ColumnDescriptor(
                        field,
                        FieldSpecifier.MODIFIED_ACTUAL
                    ),

                    ColumnDescriptor(
                        field,
                        FieldSpecifier.MODIFIED_BASELINE
                    )
                )

                FieldKind.NOTE -> listOf(
                    ColumnDescriptor(
                        field,
                        FieldSpecifier.NONE
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

        val formatter = DataFormatter()
        val defaultCharWidth = SheetUtil.getDefaultCharWidth(sheetWriter.sheet.workbook)
        val row = sheetWriter.sheet.getRow(0)

        columns.forEachIndexed { colIndex, column ->
            val cell = row.getCell(colIndex)

            val cellWidth = when (column.field.fieldKind) {
                FieldKind.NOTE -> 10000

                else -> {
                    SheetUtil.getCellWidth(cell, defaultCharWidth, formatter, false)
                        .let { it * 256 + 800 }
                        .toInt()
                        .coerceIn(0..256 * 256)
                }
            }

            sheetWriter.sheet.setColumnWidth(colIndex, cellWidth)
        }

        sheetWriter.sheet.setAutoFilter(
            CellRangeAddress(0, 0, 0, columns.size - 1)
        )

        sheetWriter.sheet.createFreezePane(0, 1)
    }

    private fun addSectionValueRows(
        columns: List<ColumnDescriptor>,
        reportSection: ReportSection,
        sheetWriter: SheetWriter
    ) {
        reportSection.changes.forEach { change ->

            val columnValues = columns.map { column ->

                val changeValue = change.fields[column.field]

                if (changeValue == null) {
                    null
                } else {
                    when (column.fieldSpecifier) {
                        FieldSpecifier.NONE -> changeValue.toString()

                        FieldSpecifier.MODIFIED_ACTUAL -> {
                            val modifiedValue = changeValue as ModifiedValue
                            modifiedValue.actualValue
                        }
                        FieldSpecifier.MODIFIED_BASELINE -> {
                            val modifiedValue = changeValue as ModifiedValue
                            modifiedValue.baselineValue
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
        return "${String.format("%02d", sectionIndex + 1)}_${sectionDescriptor.sectionShortTitle.replaceCamelCase("_")}"
    }
}
