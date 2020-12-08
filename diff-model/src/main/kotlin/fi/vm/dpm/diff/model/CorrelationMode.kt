package fi.vm.dpm.diff.model

enum class CorrelationMode {
    // Records are correlated by their keys
    CORRELATION_BY_KEY,

    // Records are correlated by their keys
    // Additions & deletions are reported only if there exists a record with matching parent key in comparison record set
    CORRELATION_BY_KEY_AND_PARENT_EXISTENCE,

    // Records are correlated by their keys and atom values
    // In case of duplicate keys, tries to detect record additions & deletions by looking matching records by their atom values.
    // Doesn't report atom value modifications.
    CORRELATION_BY_KEYS_AND_ATOMS_VALUES
}
