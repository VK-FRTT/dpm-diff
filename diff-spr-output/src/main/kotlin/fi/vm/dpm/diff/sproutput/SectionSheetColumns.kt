package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeAtomValueAdded
import fi.vm.dpm.diff.model.ChangeAtomValueDeleted
import fi.vm.dpm.diff.model.ChangeAtomValueModified
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.IdentificationLabelField
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.RecordIdentityFallbackField

object SectionSheetColumns {

    fun mapFieldsToColumns(
        sectionFields: List<Field>
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
                            headerStyle = CellStyle.HEADER_STYLE_DIMMED,
                            contentStyle = CellStyle.CONTENT_STYLE_DIMMED,
                            mapChangeValueToCell = { changeValue -> changeValue as String }
                        )
                    } else {
                        ColumnDescriptor(
                            field = field,
                            columnTitle = field.fieldName,
                            headerStyle = CellStyle.HEADER_STYLE_NORMAL,
                            contentStyle = CellStyle.CONTENT_STYLE_NORMAL,
                            mapChangeValueToCell = { changeValue -> changeValue as String }
                        )
                    }
                }

                is IdentificationLabelField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        headerStyle = CellStyle.HEADER_STYLE_NORMAL,
                        contentStyle = CellStyle.CONTENT_STYLE_NORMAL,
                        mapChangeValueToCell = { changeValue -> changeValue as String }
                    )

                is ChangeKindField ->
                    ColumnDescriptor(
                        field = field,
                        columnTitle = field.fieldName,
                        headerStyle = CellStyle.HEADER_STYLE_NORMAL,
                        contentStyle = CellStyle.CONTENT_STYLE_NORMAL,
                        mapChangeValueToCell = { changeValue -> changeValue.toString() }
                    )

                is AtomField -> {
                    listOf(
                        ColumnDescriptor(
                            field = field,
                            columnTitle = field.fieldName,
                            headerStyle = CellStyle.HEADER_STYLE_NORMAL,
                            contentStyle = CellStyle.CONTENT_STYLE_NORMAL,
                            mapChangeValueToCell = { changeValue ->
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
                            headerStyle = CellStyle.HEADER_STYLE_NORMAL,
                            contentStyle = CellStyle.CONTENT_STYLE_NORMAL,
                            mapChangeValueToCell = { changeValue ->
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
                        headerStyle = CellStyle.HEADER_STYLE_NORMAL,
                        contentStyle = CellStyle.CONTENT_STYLE_NORMAL,
                        mapChangeValueToCell = { changeValue -> changeValue as String }
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
