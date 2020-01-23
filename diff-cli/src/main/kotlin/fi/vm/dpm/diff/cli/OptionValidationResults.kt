package fi.vm.dpm.diff.cli

class OptionValidationResults {

    private val messages = mutableListOf<String>()

    fun add(subject: String, explanation: String) {
        messages.add("$subject: $explanation")
    }

    fun add(subject: String, explanation: String, input: Any) {
        messages.add("$subject: $explanation ($input)")
    }

    fun failOnErrors() {
        if (messages.any()) {
            val sb = StringBuilder()
            sb.appendln("Error:")

            messages.forEach {
                sb.appendln("- $it")
            }

            val message = sb.toString()
            throwFail(message)
        }
    }
}
