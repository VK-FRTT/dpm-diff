package fi.vm.dpm.diff.model

import ext.kotlin.isNotHavingWhitespace
import fi.vm.dpm.diff.model.diagnostic.ValidationResults

data class SectionOutline(
    val sectionShortTitle: String,
    val sectionTitle: String,
    val sectionDescription: String,
    val sectionChangeDetectionMode: ChangeDetectionMode,
    val sectionFields: List<Field>,
    val sectionSortOrder: List<SortBy>,
    val includedChanges: Set<ChangeKind>
) {

    fun validate(validationResults: ValidationResults) {
        sectionShortTitle(validationResults)
        sectionTitle(validationResults)
        sectionDescription(validationResults)
        sectionFields(validationResults)
        sectionSortOrder(validationResults)
        includedChanges(validationResults)
    }

    private fun sectionShortTitle(validationResults: ValidationResults) {
        validationResults.withSubject("SectionOutline.sectionShortTitle") {
            validateThat(
                sectionShortTitle.isNotBlank(),
                "is blank"
            )

            validateThat(
                sectionShortTitle.isNotHavingWhitespace(),
                "has whitespace"
            )
        }
    }

    private fun sectionTitle(validationResults: ValidationResults) {
        validationResults.withSubject("SectionOutline.sectionTitle") {
            validateThat(
                sectionTitle.isNotBlank(),
                "is blank"
            )
        }
    }

    private fun sectionDescription(validationResults: ValidationResults) {
        validationResults.withSubject("SectionOutline.sectionDescription") {
            validateThat(
                sectionDescription.isNotBlank(),
                "is blank"
            )
        }
    }

    private fun sectionFields(validationResults: ValidationResults) {
        validationResults.withSubject("SectionOutline.sectionFields") {

            sectionFields.forEach { field ->
                validateThat(
                    field.fieldName.isNotBlank(),
                    "fieldName is blank",
                    field.fieldName
                )
                validateThat(
                    field.fieldName.isNotHavingWhitespace(),
                    "fieldName has whitespace",
                    field.fieldName
                )
            }

            sectionFields
                .groupBy { it }
                .forEach { (_, sameFields) ->
                    validateThat(
                        sameFields.size == 1,
                        "has duplicate field",
                        sameFields.first().fieldName
                    )
                }

            validateThat(
                countOfSectionFieldsWithType(RecordIdentityFallbackField::class) == 1,
                "must have one RecordIdentityFallbackField"
            )

            validateThat(
                countOfSectionFieldsWithType(KeyField::class) >= 1,
                "must have one or more KeyField"
            )

            validateThat(
                countOfSectionFieldsWithType(ChangeKindField::class) == 1,
                "must have one ChangeKindField"
            )

            validateThat(
                countOfSectionFieldsWithType(NoteField::class) == 1,
                "must have one NoteField"
            )

            when (sectionChangeDetectionMode) {
                ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS -> {
                    validateThat(
                        countOfKeyFieldsWithKind(KeyFieldKind.PRIME_KEY) >= 1,
                        "must have one or more PRIME_KEY when ChangeDetectionMode is CORRELATE_BY_KEY_FIELDS"
                    )
                }

                ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE -> {
                    validateThat(
                        countOfKeyFieldsWithKind(KeyFieldKind.CONTEXT_PARENT_KEY) >= 1 ||
                            countOfKeyFieldsWithKind(KeyFieldKind.PARENT_KEY) >= 1,
                        "must have one or more CONTEXT_PARENT_KEY or PARENT_KEY when ChangeDetectionMode is CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE"
                    )

                    validateThat(
                        countOfKeyFieldsWithKind(KeyFieldKind.PRIME_KEY) >= 1,
                        "must have one or more PRIME_KEY when ChangeDetectionMode is CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE"
                    )
                }

                ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS -> {
                    validateThat(
                        countOfKeyFieldsWithKind(KeyFieldKind.PRIME_KEY) >= 1,
                        "must have one or more PRIME_KEY KeyField when ChangeDetectionMode is CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS"
                    )

                    validateThat(
                        countOfSectionFieldsWithType(AtomField::class) >= 1,
                        "must have one or more AtomField when ChangeDetectionMode is CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS"
                    )
                }
            }
        }
    }

    private fun sectionSortOrder(validationResults: ValidationResults) {
        validationResults.withSubject("SectionOutline.sectionSortOrder") {
            validateThat(
                sectionSortOrder.isNotEmpty(),
                "is empty"
            )

            sectionSortOrder
                .forEach { sort ->
                    validateThat(
                        sectionFields.contains(sort.field),
                        "has unknown field",
                        sort.field.fieldName
                    )
                }

            sectionSortOrder
                .groupBy { sort -> sort.field }
                .forEach { (_, sortsHavingSameField) ->
                    validateThat(
                        sortsHavingSameField.size == 1,
                        "has duplicate field",
                        sortsHavingSameField.first().field.fieldName
                    )
                }
        }
    }

    private fun includedChanges(validationResults: ValidationResults) {

        validationResults.withSubject("SectionOutline.includedChanges") {

            validateThat(
                includedChanges.isNotEmpty(),
                "is empty"
            )

            if (sectionChangeDetectionMode == ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS) {

                validateThat(
                    ChangeKind.MODIFIED !in includedChanges,
                    "must not have ChangeKind.MODIFIED when ChangeDetectionMode is CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS"
                )

                validateThat(
                    ChangeKind.DUPLICATE_KEY_ALERT !in includedChanges,
                    "must not have ChangeKind.DUPLICATE_KEY_ALERT when ChangeDetectionMode is CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS"
                )
            }
        }
    }

    private fun countOfSectionFieldsWithType(classCriteria: Any): Int {
        return sectionFields
            .filter { it::class == classCriteria }
            .size
    }

    @Suppress("UNCHECKED_CAST")
    private fun countOfKeyFieldsWithKind(kind: KeyFieldKind): Int {
        val keyFields = sectionFields
            .filter { it::class == KeyField::class } as List<KeyField>

        return keyFields
            .count { it.keyFieldKind == kind }
    }
}
