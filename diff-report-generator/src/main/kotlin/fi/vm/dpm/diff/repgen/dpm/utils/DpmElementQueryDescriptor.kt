package fi.vm.dpm.diff.repgen.dpm.utils

data class DpmElementQueryDescriptor(
    val elementType: String,
    val elementTableName: String,
    val elementIdColumn: String,
    val elementCodeColumn: String,
    val elementInherentLabelColumn: String,
    val parentType: String,
    val parentCodeStatement: String,
    val parentTableJoin: String,
    val elementTableSliceCriteria: String
)
