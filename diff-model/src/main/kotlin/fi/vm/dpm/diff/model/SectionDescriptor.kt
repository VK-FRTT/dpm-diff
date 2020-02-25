package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType
import ext.kotlin.isNotHavingWhitespace

data class SectionDescriptor(
    val sectionShortTitle: String,
    val sectionTitle: String,
    val sectionDescription: String,
    val sectionFields: List<Field>,
    val sectionSortOrder: List<Sort>,
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
            check(filterFieldType<Field, CorrelationKeyField>().size >= 1)
            check(filterFieldType<Field, FallbackField>().size >= 0)
            check(filterFieldType<Field, IdentificationLabelField>().size >= 1)
            check(filterFieldType<Field, ChangeKindField>().size == 1)
            check(filterFieldType<Field, AtomField>().size >= 0)
            check(filterFieldType<Field, NoteField>().size == 1)

            // FieldName:
            // - Should not be empty nor contain whitespace (i.e. it should be in CamelCase)
            forEach { field ->
                check(field.fieldName.isNotBlank())
                check(field.fieldName.isNotHavingWhitespace())
            }
        }

        // sectionSortOrder
        check(sectionSortOrder.isNotEmpty())
        sectionSortOrder.forEach { sort ->
            check(sectionFields.contains(sort.field))
        }

        // correlationMode
        when (correlationMode) {
            CorrelationMode.UNINITIALIZED -> {
                check(false)
            }

            CorrelationMode.ONE_PHASE_BY_FULL_KEY -> {
                check(sectionFields
                    .filterFieldType<Field, CorrelationKeyField>()
                    .all { field -> field.correlationKeyKind == CorrelationKeyKind.PRIMARY_KEY })
            }

            CorrelationMode.TWO_PHASE_BY_PRIMARY_AND_FULL_KEY -> {
                check(sectionFields
                    .filterFieldType<Field, CorrelationKeyField>()
                    .any { field -> field.correlationKeyKind == CorrelationKeyKind.PRIMARY_KEY })

                check(sectionFields
                    .filterFieldType<Field, CorrelationKeyField>()
                    .any { field -> field.correlationKeyKind == CorrelationKeyKind.SECONDARY_KEY })
            }
        }

        with(includedChanges) {
            check(isNotEmpty())
        }
    }
}
