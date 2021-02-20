package fi.vm.dpm.diff.model

enum class ChangeDetectionMode {

    // Records are correlated by their key field values
    // Expects that there are no duplicate records (i.e. combined keys are unique)
    CORRELATE_BY_KEY_FIELDS,

    // Records are correlated by their key field values
    // Expects that there are no duplicate records (i.e. combined keys are unique)
    // Additions & deletions are reported only for records which:
    // - A) There exists "a parent record" in comparison record set
    //   (i.e. comparison record set must contain a record with matching
    //   CONTEXT_PARENT_KEY and PARENT_KEY values)
    // - B) PRIME_KEY is not completely NULL
    //   (such records are needed in record set for condition A) to work)
    CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE,

    // Records are correlated first by their key field values and then by atom values
    // Tolerates duplicate records (i.e. combined keys may be same)
    // In case of duplicate keys, tries to detect record additions & deletions by looking corresponding record by their atom values.
    // Doesn't report atom value modifications.
    CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS
}
