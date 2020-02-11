package fi.vm.dpm.diff.sproutput

import ext.kotlin.replaceCamelCase
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.thisShouldNeverHappen

data class ColumnDescriptor(
    val field: FieldDescriptor,
    val fieldSpecifier: FieldSpecifier
) {
    fun title(): String {
        return when (field.fieldKind) {
            FieldKind.FALLBACK_VALUE -> thisShouldNeverHappen("No column for fallback fields")
            FieldKind.CORRELATION_KEY -> field.fieldName
            FieldKind.IDENTIFICATION_LABEL -> field.fieldName
            FieldKind.DIFFERENCE_KIND -> field.fieldName
            FieldKind.ATOM -> {
                if (fieldSpecifier == FieldSpecifier.CHANGE_BASELINE) {
                    "${field.fieldName} (baseline)"
                } else {
                    field.fieldName
                }
            }
            FieldKind.NOTE -> field.fieldName
        }.replaceCamelCase().toUpperCase()
    }
}
