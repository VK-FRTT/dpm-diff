package fi.vm.dpm.diff.model

import ext.kotlin.isNotHavingWhitespace

data class SectionDescriptor(
    val sectionShortTitle: String,
    val sectionTitle: String,
    val sectionDescription: String,
    val sectionFields: List<FieldDescriptor>
) {
    fun sanityCheck() {
        with(sectionShortTitle) {
            check(isNotBlank())
            check(isNotHavingWhitespace())
        }

        with(sectionTitle) {
            check(isNotBlank())
        }

        with(sectionDescription) {
            check(isNotBlank())
        }

        with(sectionFields) {

            check(isNotEmpty())

            // Field amounts are restricted per field kind
            check(count { it.fieldKind == FieldKind.CORRELATION_KEY } > 0)
            check(count { it.fieldKind == FieldKind.IDENTIFICATION_LABEL } > 0)
            check(count { it.fieldKind == FieldKind.DIFFERENCE_KIND } == 1)
            check(count { it.fieldKind == FieldKind.NOTE } == 1)

            // Fallbacks are restricted for certain field kinds only
            forEach { field ->

                if (field.correlationKeyFallback != null) {
                    check(field.fieldKind == FieldKind.CORRELATION_KEY)
                }

                if (field.noteFields.any()) {
                    check(
                        field.fieldKind in listOf(
                            FieldKind.CORRELATION_KEY,
                            FieldKind.IDENTIFICATION_LABEL
                        )
                    )
                }
            }

            // Fallbacks can refer only fields with fallback kind
            forEach { field ->

                with(field.correlationKeyFallback) {
                    check(this == null || this.fieldKind == FieldKind.FALLBACK_VALUE)
                }

                field.noteFields.forEach { noteField ->
                    check(noteField.fieldKind == FieldKind.FALLBACK_VALUE)
                }
            }

            // Field name should not be empty nor contain whitespace (i.e. it should be in CamelCase)
            forEach { field ->
                check(field.fieldName.isNotBlank())
                check(field.fieldName.isNotHavingWhitespace())
            }
        }
    }
}
