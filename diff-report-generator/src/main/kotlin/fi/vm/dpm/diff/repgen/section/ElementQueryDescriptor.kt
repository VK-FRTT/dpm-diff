package fi.vm.dpm.diff.repgen.section

data class ElementQueryDescriptor(
    val elementType: String,
    val elementTableName: String,
    val elementIdColumn: String,
    val elementCodeColumn: String,
    val elementInherentLabelColumn: String,
    val parentCodeStatement: String,
    val parentTableJoin: String,
    val elementTableSliceCriteria: String
)
