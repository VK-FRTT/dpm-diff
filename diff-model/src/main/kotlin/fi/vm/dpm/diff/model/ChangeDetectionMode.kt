package fi.vm.dpm.diff.model

enum class ChangeDetectionMode {
    // Records are correlated by their key field values
    // Expects that there are no duplicate records (i.e. combined keys are unique)
    CORRELATE_BY_KEY_FIELDS,

    // Records are correlated by their key field values
    // Expects that there are no duplicate records (i.e. combined keys are unique)
    // Additions & deletions are reported only if there exists a record with matching parent key in comparison record set
    CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE,

    // Records are correlated first by their key field values and then by atom values
    // Tolerates duplicate records (i.e. combined keys may be same)
    // In case of duplicate keys, tries to detect record additions & deletions by looking corresponding record by their atom values.
    // Doesn't report atom value modifications.
    CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS
}
