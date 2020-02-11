package fi.vm.dpm.diff.model

data class SectionDescriptor(
    val sectionShortTitle: String,
    val sectionTitle: String,
    val sectionDescription: String,
    val sectionFields: List<FieldDescriptor>
) {
    fun sanityCheck() {
        with(sectionShortTitle) {
            check(isNotBlank())
            check(!contains(" "))
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
            check(count { it.fieldKind == fi.vm.dpm.diff.model.FieldKind.CORRELATION_KEY } > 0)
            check(count { it.fieldKind == fi.vm.dpm.diff.model.FieldKind.IDENTIFICATION_LABEL } > 0)
            check(count { it.fieldKind == fi.vm.dpm.diff.model.FieldKind.DIFFERENCE_KIND } == 1)
            check(count { it.fieldKind == fi.vm.dpm.diff.model.FieldKind.NOTE } == 1)

            // Fallbacks are restricted for certain field kinds only
            forEach { sectionField ->

                if (sectionField.correlationKeyFallback != null) {
                    check(sectionField.fieldKind == fi.vm.dpm.diff.model.FieldKind.CORRELATION_KEY)
                }

                if (sectionField.noteFallback.any()) {
                    check(
                        sectionField.fieldKind in listOf(
                            fi.vm.dpm.diff.model.FieldKind.CORRELATION_KEY,
                            fi.vm.dpm.diff.model.FieldKind.IDENTIFICATION_LABEL
                        )
                    )
                }
            }

            // Fallbacks can refer only fields with fallback kind
            forEach { sectionField ->

                with(sectionField.correlationKeyFallback) {
                    check(this == null || this.fieldKind == fi.vm.dpm.diff.model.FieldKind.FALLBACK_VALUE)
                }

                sectionField.noteFallback.forEach { noteFallbackField ->
                    check(noteFallbackField.fieldKind == fi.vm.dpm.diff.model.FieldKind.FALLBACK_VALUE)
                }
            }

            // Field name should not be empty nor contain space (i.e. it should be in CamelCase)
            forEach { sectionField ->
                check(sectionField.fieldName.isNotBlank())
                check(!sectionField.fieldName.contains(" "))
            }
        }
    }
}
