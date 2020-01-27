package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.FieldDescriptor

data class ColumnDescriptor(
    val columnKind: ColumnKind,
    val field: FieldDescriptor

) {
    fun title(): String {
        return when (columnKind) {
            ColumnKind.CORRELATION_ID -> field.fieldName
            ColumnKind.IDENTIFICATION_LABEL -> field.fieldName
            ColumnKind.DIFFERENCE_KIND -> field.fieldName
            ColumnKind.CHANGE_ACTUAL -> field.fieldName
            ColumnKind.CHANGE_BASELINE -> "${field.fieldName} (baseline)"
        }.toUpperCase()
    }
}
