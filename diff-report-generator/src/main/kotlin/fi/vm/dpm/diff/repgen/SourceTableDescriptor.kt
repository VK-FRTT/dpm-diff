package fi.vm.dpm.diff.repgen

data class SourceTableDescriptor(
    val table: String,
    val where: String? = null,
    val join: String? = null
)
