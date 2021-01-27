package fi.vm.dpm.diff.model.diagnostic

interface Diagnostic {

    fun info(message: String)
    fun verbose(message: String)
    fun debug(message: String)
    fun fatal(message: String): Nothing
    fun progressIndication(): ProgressIndication
}
