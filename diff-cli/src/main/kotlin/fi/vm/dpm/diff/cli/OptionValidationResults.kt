package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.Diagnostic

class OptionValidationResults {

    private val messages = mutableListOf<String>()

    fun add(subject: String, explanation: String) {
        messages.add("$subject: $explanation")
    }

    fun add(subject: String, explanation: String, input: Any) {
        messages.add("$subject: $explanation ($input)")
    }

    fun reportErrors(diagnostic: Diagnostic) {
        if (messages.any()) {

            val message = messages.joinToString(
                separator = "\n- ",
                prefix = "- "
            )
            diagnostic.fatal(message)
        }
    }
}
