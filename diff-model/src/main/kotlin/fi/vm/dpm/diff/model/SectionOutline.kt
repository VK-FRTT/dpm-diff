package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType
import ext.kotlin.isNotHavingWhitespace

data class SectionOutline(
    val sectionShortTitle: String,
    val sectionTitle: String,
    val sectionDescription: String,
    val sectionChangeDetectionMode: ChangeDetectionMode,
    val sectionFields: List<Field>,
    val sectionSortOrder: List<SortBy>,
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

        // sectionCorrelationMode
        run {
            val keyFields = sectionFields.filterFieldType<KeyField>()

            when (sectionChangeDetectionMode) {
                ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS -> {
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.CONTEXT_PARENT_KEY } >= 0)
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.PARENT_KEY } >= 0)
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.PRIME_KEY } >= 1)
                }

                ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE -> {
                    check(
                        keyFields.count { it.keyFieldKind == KeyFieldKind.CONTEXT_PARENT_KEY } >= 1 ||
                        keyFields.count { it.keyFieldKind == KeyFieldKind.PARENT_KEY } >= 1
                    )
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.PRIME_KEY } >= 1)
                }

                ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS -> {
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.CONTEXT_PARENT_KEY } >= 0)
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.PARENT_KEY } >= 0)
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.PRIME_KEY } >= 1)
                    check(sectionFields.filterFieldType<AtomField>().size >= 1)

                    check(ChangeKind.MODIFIED !in includedChanges)
                    check(ChangeKind.DUPLICATE_KEY_ALERT !in includedChanges)
                }
            }
        }

        with(sectionFields) {
            check(isNotEmpty())

            // Field amounts per field kind
            check(filterFieldType<FallbackField>().size >= 0)
            check(filterFieldType<RecordIdentityFallbackField>().size == 1)
            check(filterFieldType<KeyField>().size >= 1)
            check(filterFieldType<IdentificationLabelField>().size >= 0)
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

    private fun check(value: Boolean) {
        if (!value) {
            thisShouldNeverHappen("SectionOutline SanityCheck failed for: $sectionTitle")
        }
    }
}
