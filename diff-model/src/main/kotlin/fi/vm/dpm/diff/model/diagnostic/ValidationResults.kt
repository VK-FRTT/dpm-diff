package fi.vm.dpm.diff.model.diagnostic

class ValidationResults {

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

    fun messages(): List<String> = messages

    fun withSubject(subject: String, execute: SubjectValidator.() -> Unit) {
        val validator = SubjectValidator(subject, this)
        validator.execute()
    }

    class SubjectValidator internal constructor(
        private val subject: String,
        private val validationResults: ValidationResults
    ) {

        fun validateThat(test: Boolean, explanation: String) {
            if (!test) {
                validationResults.add(subject, explanation)
            }
        }

        fun validateThat(test: Boolean, explanation: String, input: Any) {
            if (!test) {
                validationResults.add(subject, explanation, input)
            }
        }
    }
}
