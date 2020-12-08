package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType
import ext.kotlin.isNotHavingWhitespace

data class SectionOutline(
    val sectionShortTitle: String,
    val sectionTitle: String,
    val sectionDescription: String,
    val sectionCorrelationMode: CorrelationMode,
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

        // sectionCorrelationMode
        run {
            val keyFields = sectionFields.filterFieldType<KeyField>()

            when (sectionCorrelationMode) {
                CorrelationMode.CORRELATION_BY_KEY -> {
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.CONTEXT_PARENT_KEY } >= 0)
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.PARENT_KEY } >= 0)
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.PRIME_KEY } >= 1)
                }

                CorrelationMode.CORRELATION_BY_KEY_AND_PARENT_EXISTENCE -> {
                    check(
                        keyFields.count { it.keyFieldKind == KeyFieldKind.CONTEXT_PARENT_KEY } >= 1 ||
                        keyFields.count { it.keyFieldKind == KeyFieldKind.PARENT_KEY } >= 1
                    )
                    check(keyFields.count { it.keyFieldKind == KeyFieldKind.PRIME_KEY } >= 1)
                }

                CorrelationMode.CORRELATION_BY_KEYS_AND_ATOMS_VALUES -> {
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
