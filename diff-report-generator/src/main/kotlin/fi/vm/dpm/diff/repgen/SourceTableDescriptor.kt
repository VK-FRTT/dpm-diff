package fi.vm.dpm.diff.repgen

data class SourceTableDescriptor(
    val table: String,
    val joins: List<String>? = null,
    val where: String? = null
)
