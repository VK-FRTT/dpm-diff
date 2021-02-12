package fi.vm.dpm.diff.model.diagnostic

class DiagnosticCollector : Diagnostic {
    val messages = mutableListOf<String>()

    override fun info(message: String) {
        messages.add("INFO: $message")
    }

    override fun verbose(message: String) {
        messages.add("VERBOSE: $message")
    }

    override fun debug(message: String) {
        messages.add("DEBUG: $message")
    }

    override fun fatal(message: String): Nothing {
        messages.add("FATAL: $message")
        throw ArithmeticException(message)
    }

    override fun progressIndication(): ProgressIndication {
        return object : ProgressIndication {

            override fun handleStep() {
                messages.add("PI STEP")
            }

            override fun handleDone() {
                messages.add("PI DONE")
            }
        }
    }
}
