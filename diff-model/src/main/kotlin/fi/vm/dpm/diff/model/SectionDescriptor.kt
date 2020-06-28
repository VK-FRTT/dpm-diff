package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType
import ext.kotlin.isNotHavingWhitespace

data class SectionDescriptor(
    val sectionShortTitle: String,
    val sectionTitle: String,
    val sectionDescription: String,
    val sectionFields: List<Field>,
    val sectionSortOrder: List<Sort>,
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
            check(filterFieldType<FallbackField>().size >= 0)
            check(filterFieldType<RecordIdentityFallbackField>().size == 1)
            check(filterFieldType<KeySegmentField>().size >= 1)
            check(filterFieldType<IdentificationLabelField>().size >= 1)
            check(filterFieldType<ChangeKindField>().size == 1)
            check(filterFieldType<AtomField>().size >= 0)
            check(filterFieldType<NoteField>().size == 1)

            // FieldName:
            // - Should not be empty nor contain whitespace (i.e. it should be in CamelCase)
            forEach { field ->
                check(field.fieldName.isNotBlank())
                check(field.fieldName.isNotHavingWhitespace())
            }

            // Field identity uniqueness
            sectionFields
                .groupBy { field -> field }
                .forEach { (_, sameFields) ->
                    check(sameFields.size == 1)
                }
        }

        // sectionSortOrder
        check(sectionSortOrder.isNotEmpty())

        sectionSortOrder.forEach { sort ->
            check(sectionFields.contains(sort.field))
        }

        sectionSortOrder
            .groupBy { sort -> sort.field }
            .forEach { (_, sortsHavingSameField) ->
                check(sortsHavingSameField.size == 1)
            }

        with(includedChanges) {
            check(isNotEmpty())
        }
    }
}
