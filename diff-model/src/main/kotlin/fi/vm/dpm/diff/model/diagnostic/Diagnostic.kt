package fi.vm.dpm.diff.model.diagnostic

interface Diagnostic {

    fun info(message: String)
    fun infoStepProgress()
    fun verbose(message: String)
    fun debug(message: String)
    fun fatal(message: String): Nothing
}
