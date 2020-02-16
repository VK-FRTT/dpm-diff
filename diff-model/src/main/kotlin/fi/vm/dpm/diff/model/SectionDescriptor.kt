package fi.vm.dpm.diff.model

import ext.kotlin.isNotHavingWhitespace

data class SectionDescriptor(
    val sectionShortTitle: String,
    val sectionTitle: String,
    val sectionDescription: String,
    val sectionFields: List<FieldDescriptor>,
    val correlationMode: CorrelationMode,
    val includedChanges: Set<ChangeKind>
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

            // Field amounts per field kind
            check(count { it.fieldKind == FieldKind.CORRELATION_KEY } >= 1)
            check(count { it.fieldKind == FieldKind.FALLBACK_VALUE } >= 0)
            check(count { it.fieldKind == FieldKind.IDENTIFICATION_LABEL } >= 1)
            check(count { it.fieldKind == FieldKind.CHANGE_KIND } == 1)
            check(count { it.fieldKind == FieldKind.ATOM } >= 0)
            check(count { it.fieldKind == FieldKind.NOTE } == 1)

            // FieldName:
            // - Should not be empty nor contain whitespace (i.e. it should be in CamelCase)
            forEach { field ->
                check(field.fieldName.isNotBlank())
                check(field.fieldName.isNotHavingWhitespace())
            }

            // CorrelationKeyKind:
            // - For CORRELATION_KEY fields it must have proper value
            // - For other fields it must remain as UNINITIALIZED
            forEach { field ->
                if (field.fieldKind == FieldKind.CORRELATION_KEY) {
                    check(field.correlationKeyKind != CorrelationKeyKind.UNINITIALIZED)
                }

                if (field.fieldKind != FieldKind.CORRELATION_KEY) {
                    check(field.correlationKeyKind == CorrelationKeyKind.UNINITIALIZED)
                }
            }

            // CorrelationFallback:
            // - Is allowed only for CORRELATION_KEY fields
            // - Can refer only FALLBACK_VALUE fields
            forEach { field ->
                if (field.correlationFallback != null) {
                    check(field.fieldKind == FieldKind.CORRELATION_KEY)
                    check(field.correlationFallback.fieldKind == FieldKind.FALLBACK_VALUE)
                }
            }

            // NoteFields:
            // - Is allowed only for CORRELATION_KEY & IDENTIFICATION_LABEL fields
            // - Can refer only FALLBACK_VALUE fields
            forEach { field ->
                if (field.noteFields.any()) {
                    check(
                        field.fieldKind in listOf(
                            FieldKind.CORRELATION_KEY,
                            FieldKind.IDENTIFICATION_LABEL
                        )
                    )
                }

                field.noteFields.forEach { noteField ->
                    check(noteField.fieldKind == FieldKind.FALLBACK_VALUE)
                }
            }
        }

        // correlationMode
        when (correlationMode) {
            CorrelationMode.UNINITIALIZED -> {
                check(false)
            }

            CorrelationMode.ONE_PHASE_BY_FULL_KEY -> {
                check(sectionFields
                    .filter { field -> field.fieldKind == FieldKind.CORRELATION_KEY }
                    .all { field -> field.correlationKeyKind == CorrelationKeyKind.PRIMARY_KEY })
            }

            CorrelationMode.TWO_PHASE_BY_PRIMARY_AND_FULL_KEY -> {
                check(sectionFields
                    .filter { field -> field.fieldKind == FieldKind.CORRELATION_KEY }
                    .any { field -> field.correlationKeyKind == CorrelationKeyKind.PRIMARY_KEY })

                check(sectionFields
                    .filter { field -> field.fieldKind == FieldKind.CORRELATION_KEY }
                    .any { field -> field.correlationKeyKind == CorrelationKeyKind.SECONDARY_KEY })
            }
        }

        with(includedChanges) {
            check(isNotEmpty())
        }
    }
}
