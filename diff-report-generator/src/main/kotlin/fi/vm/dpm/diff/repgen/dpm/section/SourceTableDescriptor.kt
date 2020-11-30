package fi.vm.dpm.diff.repgen.dpm.section

data class SourceTableDescriptor(
    val table: String,
    val joins: List<String>? = null,
    val where: String? = null
)
