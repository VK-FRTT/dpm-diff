package fi.vm.dpm.diff.repgen.dpm.utils

data class SourceTableDescriptor(
    val table: String,
    val joins: List<String>? = null,
    val where: String? = null
)
