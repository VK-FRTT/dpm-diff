package fi.vm.dpm.diff.model

enum class ChangeKind {
    ADDED,
    DELETED,
    MODIFIED,
    DUPLICATE_KEY;
    companion object {
        fun allValues() = ChangeKind.values().toSet()
    }
}
