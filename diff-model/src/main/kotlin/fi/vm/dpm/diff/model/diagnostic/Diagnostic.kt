package fi.vm.dpm.diff.model.diagnostic

interface Diagnostic {
    fun fatal(message: String): Nothing
    fun info(message: String)
    fun debug(message: String)
}
