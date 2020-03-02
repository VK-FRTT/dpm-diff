package fi.vm.dpm.diff.model

import ext.kotlin.replaceCamelCase
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.sproutput.CellStyles
import fi.vm.dpm.diff.sproutput.ColumnDescriptor
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
        sw.addRow("Current database", changeReport.currentDpmDbFileName)

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

    @Suppress("UNCHECKED_CAST")
    private fun composeSectionColumns(
        reportSection: ReportSection
    ): List<ColumnDescriptor> {

        return reportSection.sectionDescriptor.sectionFields.flatMap { field ->

            val fieldColumns: Any? = when (field) {

                is CorrelationKeyField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        toColumnValue = { changeValue -> changeValue as String }
                    )

                is FallbackField -> null

                is IdentificationLabelField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        toColumnValue = { changeValue -> changeValue as String }
                    )

                is ChangeKindField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        toColumnValue = { changeValue -> changeValue.toString() }
                    )

                is AtomField -> listOf(
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        toColumnValue = { changeValue ->
                            when (changeValue) {
                                is AddedChangeAtomValue -> changeValue.value
                                is ModifiedChangeAtomValue -> changeValue.currentValue
                                else -> null
                            }
                        }
                    ),

                    ColumnDescriptor(
                        field = field,
                        columnTitle = "${field.fieldName} (baseline)",
                        toColumnValue = { changeValue ->
                            when (changeValue) {
                                is AddedChangeAtomValue -> null
                                is ModifiedChangeAtomValue -> changeValue.baselineValue
                                else -> null
                            }
                        }
                    )
                )

                is NoteField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        toColumnValue = { changeValue -> changeValue as String }
                    )
            }

            when (fieldColumns) {
                is ColumnDescriptor -> listOf(fieldColumns)
                is List<*> -> fieldColumns as List<ColumnDescriptor>
                else -> emptyList()
            }
        }
    }

    private fun addSectionTitleRow(
        columns: List<ColumnDescriptor>,
        sheetWriter: SheetWriter
    ) {
        val titles = columns
            .map { it.columnTitle.replaceCamelCase().toUpperCase() }
            .toTypedArray()

        sheetWriter.addHeaderRow(*titles)

        val formatter = DataFormatter()
        val defaultCharWidth = SheetUtil.getDefaultCharWidth(sheetWriter.sheet.workbook)
        val row = sheetWriter.sheet.getRow(0)

        columns.forEachIndexed { colIndex, column ->
            val cell = row.getCell(colIndex)

            val cellWidth = when (column.field) {
                is NoteField -> 10000

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
                    column.toColumnValue(changeValue)
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
