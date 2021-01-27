package fi.vm.dpm.diff.model.diagnostic

interface ProgressIndication {
    fun handleStep()
    fun handleDone()
}
