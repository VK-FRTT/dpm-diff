package fi.vm.dpm.diff.sproutput

import ext.kotlin.replaceCamelCase
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeAtomValueAdded
import fi.vm.dpm.diff.model.ChangeAtomValueDeleted
import fi.vm.dpm.diff.model.ChangeAtomValueModified
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.DisplayHint
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.IdentificationLabelField
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.ReportSection
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.model.thisShouldNeverHappen
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

        val sw = SheetWriter.createToWorkbook(sheetName, workbook)

        val columns = sectionColumns(
            section.sectionOutline.sectionFields,
            cellStyles
        )

        val headerCells = columns.map {
            Triple(
                first = it.columnTitle.replaceCamelCase().toUpperCase(),
                second = it.headerStyle,
                third = it.columnWidth
            )
        }
        sw.addHeaderRow(headerCells)

        section.changes.forEach { changeRecord ->
            val contentCells = columns
                .map { column ->
                    Pair(
                        first = changeRecord.fields[column.field]?.let { column.changeToCellValue(it) },
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
        return "${String.format("%02d", sectionIndex + 1)}_${sectionOutline.sectionShortTitle.replaceCamelCase("_")}"
    }

    private fun sectionColumns(
        sectionFields: List<Field>,
        cellStyles: CellStyles
    ): List<ColumnDescriptor> {

        return sectionFields.flatMap { field ->

            val fieldColumns: Any? = when (field) {

                is FallbackField -> null

                is RecordIdentityFallbackField -> null

                is KeyField -> {
                    if (field.keyFieldKind == KeyFieldKind.CONTEXT_PARENT_KEY) {
                        ColumnDescriptor(
                            field = field,
                            columnTitle = field.fieldName,
                            columnWidth = field.displayHintToColumnWidth(),
                            headerStyle = cellStyles.headerStyleDimmed,
                            contentStyle = cellStyles.contentStyleDimmed,
                            changeToCellValue = { changeValue -> changeValue as String }
                        )
                    } else {
                        ColumnDescriptor(
                            field = field,
                            columnTitle = field.fieldName,
                            columnWidth = field.displayHintToColumnWidth(),
                            headerStyle = cellStyles.headerStyleNormal,
                            contentStyle = cellStyles.contentStyleNormal,
                            changeToCellValue = { changeValue -> changeValue as String }
                        )
                    }
                }

                is IdentificationLabelField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        columnWidth = field.displayHintToColumnWidth(),
                        headerStyle = cellStyles.headerStyleNormal,
                        contentStyle = cellStyles.contentStyleNormal,
                        changeToCellValue = { changeValue -> changeValue as String }
                    )

                is ChangeKindField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        columnWidth = field.displayHintToColumnWidth(),
                        headerStyle = cellStyles.headerStyleNormal,
                        contentStyle = cellStyles.contentStyleNormal,
                        changeToCellValue = { changeValue -> changeValue.toString() }
                    )

                is AtomField -> {
                    listOf(
                        ColumnDescriptor(
                            field = field,
                            columnTitle = field.fieldName,
                            columnWidth = field.displayHintToColumnWidth(),
                            headerStyle = cellStyles.headerStyleNormal,
                            contentStyle = cellStyles.contentStyleNormal,
                            changeToCellValue = { changeValue ->
                                when (changeValue) {
                                    is ChangeAtomValueAdded -> changeValue.value
                                    is ChangeAtomValueDeleted -> null
                                    is ChangeAtomValueModified -> changeValue.currentValue
                                    else -> null
                                }
                            }
                        ),

                        ColumnDescriptor(
                            field = field,
                            columnTitle = "${field.fieldName} (baseline)",
                            columnWidth = field.displayHintToColumnWidth(),
                            headerStyle = cellStyles.headerStyleNormal,
                            contentStyle = cellStyles.contentStyleNormal,
                            changeToCellValue = { changeValue ->
                                when (changeValue) {
                                    is ChangeAtomValueAdded -> null
                                    is ChangeAtomValueDeleted -> changeValue.value
                                    is ChangeAtomValueModified -> changeValue.baselineValue
                                    else -> null
                                }
                            }
                        )
                    )
                }

                is NoteField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        columnWidth = field.displayHintToColumnWidth(),
                        headerStyle = cellStyles.headerStyleNormal,
                        contentStyle = cellStyles.contentStyleNormal,
                        changeToCellValue = { changeValue -> changeValue as String }
                    )
            }

            @Suppress("UNCHECKED_CAST")
            when (fieldColumns) {
                is ColumnDescriptor -> listOf(fieldColumns)
                is List<*> -> fieldColumns as List<ColumnDescriptor>
                else -> emptyList()
            }
        }
    }

    private fun Field.displayHintToColumnWidth(): ColumnWidth {
        return when (displayHint) {
            DisplayHint.FIT_BY_TITLE -> {
                ColumnWidth.FIT_TITLE_CONTENT_WITH_MARGIN
            }

            DisplayHint.FIXED_WIDE -> {
                ColumnWidth.FIXED_WIDE
            }

            DisplayHint.FIXED_EXTRA_WIDE -> {
                ColumnWidth.FIXED_EXTRA_WIDE
            }

            else -> {
                thisShouldNeverHappen("Unsupported display hint")
            }
        }
    }
}
