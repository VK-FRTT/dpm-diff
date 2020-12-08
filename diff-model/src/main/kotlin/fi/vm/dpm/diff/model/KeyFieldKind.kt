package fi.vm.dpm.diff.model

enum class KeyFieldKind {
    CONTEXT_PARENT_KEY, // Key identifies grand parent or ancestor of the correlated object for contextual purposes
    PARENT_KEY, // Key identifies direct parent of the correlated object
    PRIME_KEY // Key identifies the correlated object itself
}
