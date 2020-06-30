package fi.vm.dpm.diff.model

enum class KeySegmentKind {
    SCOPE_SEGMENT, // Key segment identifies parent or ancestor of the correlated object
    PRIME_SEGMENT, // Key segment identifies the correlated object itself
    SUB_SEGMENT, // Key segment identifies the sub-object within the context of the correlated object
}
