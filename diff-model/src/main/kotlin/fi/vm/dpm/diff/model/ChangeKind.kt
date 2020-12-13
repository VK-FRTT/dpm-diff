package fi.vm.dpm.diff.model

enum class ChangeKind {
    ADDED,
    DELETED,
    MODIFIED,
    DUPLICATE_KEY_ALERT;

    companion object {
        fun additionAndDeletion() = setOf(
            ADDED,
            DELETED
        )

        fun allChanges() = setOf(
            ADDED,
            DELETED,
            MODIFIED,
            DUPLICATE_KEY_ALERT
        )

        fun allExceptDuplicateKeyAlerts() = setOf(
            ADDED,
            DELETED,
            MODIFIED
        )
    }
}
