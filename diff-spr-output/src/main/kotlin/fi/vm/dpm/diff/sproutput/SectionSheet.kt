package fi.vm.dpm.diff.sproutput

import ext.kotlin.replaceCamelCase
import fi.vm.dpm.diff.model.AddedChangeAtomValue
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.IdentificationLabelField
import fi.vm.dpm.diff.model.KeySegmentField
import fi.vm.dpm.diff.model.KeySegmentKind
import fi.vm.dpm.diff.model.ModifiedChangeAtomValue
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.ReportSection
import fi.vm.dpm.diff.model.SectionDescriptor
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
            section.sectionDescriptor
        )

        val sw = SheetWriter.createToWorkbook(sheetName, workbook)

        val columns = sectionColumns(
            section.sectionDescriptor.sectionFields,
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
        sectionDescriptor: SectionDescriptor
    ): String {
        return "${String.format("%02d", sectionIndex + 1)}_${sectionDescriptor.sectionShortTitle.replaceCamelCase("_")}"
    }

    private fun sectionColumns(
        sectionFields: List<Field>,
        cellStyles: CellStyles
    ): List<ColumnDescriptor> {

        return sectionFields.flatMap { field ->

            val fieldColumns: Any? = when (field) {

                is FallbackField -> null

                is RecordIdentityFallbackField -> null

                is KeySegmentField -> {
                    if (field.segmentKind == KeySegmentKind.SCOPE_SEGMENT) {
                        ColumnDescriptor(
                            field = field,
                            columnTitle = field.fieldName,
                            columnWidth = ColumnWidth.FIT_TITLE_CONTENT_WITH_MARGIN,
                            headerStyle = cellStyles.headerStyleDimmed,
                            contentStyle = cellStyles.contentStyleDimmed,
                            changeToCellValue = { changeValue -> changeValue as String }
                        )
                    } else {
                        ColumnDescriptor(
                            field = field,
                            columnTitle = field.fieldName,
                            columnWidth = ColumnWidth.FIT_TITLE_CONTENT_WITH_MARGIN,
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
                        columnWidth = ColumnWidth.FIT_TITLE_CONTENT_WITH_MARGIN,
                        headerStyle = cellStyles.headerStyleNormal,
                        contentStyle = cellStyles.contentStyleNormal,
                        changeToCellValue = { changeValue -> changeValue as String }
                    )

                is ChangeKindField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        columnWidth = ColumnWidth.FIT_TITLE_CONTENT_WITH_MARGIN,
                        headerStyle = cellStyles.headerStyleNormal,
                        contentStyle = cellStyles.contentStyleNormal,
                        changeToCellValue = { changeValue -> changeValue.toString() }
                    )

                is AtomField -> {
                    val columnWidth = if (field.fieldName.toLowerCase().contains("translation")) {
                        ColumnWidth.FIXED_EXTRA_WIDE
                    } else {
                        ColumnWidth.FIT_TITLE_CONTENT_WITH_MARGIN
                    }

                    listOf(
                        ColumnDescriptor(
                            field = field,
                            columnTitle = field.fieldName,
                            columnWidth = columnWidth,
                            headerStyle = cellStyles.headerStyleNormal,
                            contentStyle = cellStyles.contentStyleNormal,
                            changeToCellValue = { changeValue ->
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
                            columnWidth = columnWidth,
                            headerStyle = cellStyles.headerStyleNormal,
                            contentStyle = cellStyles.contentStyleNormal,
                            changeToCellValue = { changeValue ->
                                when (changeValue) {
                                    is AddedChangeAtomValue -> null
                                    is ModifiedChangeAtomValue -> changeValue.baselineValue
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
                        columnWidth = ColumnWidth.FIXED_WIDE,
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
}
