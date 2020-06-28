package fi.vm.dpm.diff.model

enum class KeySegmentKind {
    SCOPING_TOP_LEVEL_SEGMENT, // Key segment identifies parent or ancestor of the correlated object
    TOP_LEVEL_SEGMENT, // Key segment identifies the correlated object itself
    SUB_OBJECT_SEGMENT, // Key segment identifies the sub-object within the context of the correlated object
}
