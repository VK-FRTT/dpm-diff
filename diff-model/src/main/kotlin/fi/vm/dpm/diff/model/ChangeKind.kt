package fi.vm.dpm.diff.model

enum class ChangeKind {
    ADDED,
    DELETED,
    MODIFIED;

    companion object {
        fun allValues() = ChangeKind.values().toSet()
    }
}
