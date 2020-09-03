package fi.vm.dpm.diff.model

enum class ChangeKind {
    ADDED,
    DELETED,
    MODIFIED,
    DUPLICATE_KEY;
    
    companion object {
        fun allChanges() = setOf(
            ADDED,
            DELETED,
            MODIFIED,
            DUPLICATE_KEY
        )

        fun allWithoutDuplicateKeyChanges() = setOf(
            ADDED,
            DELETED,
            MODIFIED
        )
    }
}
