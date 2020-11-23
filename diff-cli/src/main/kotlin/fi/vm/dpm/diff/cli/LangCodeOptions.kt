package fi.vm.dpm.diff.cli

object LangCodeOptions {

    fun checkIdentificationLabelLanguages(
        identificationLabelLanguages: String?,
        optName: OptName,
        validationResults: OptionValidationResults
    ): List<String> {

        return if (identificationLabelLanguages == null) {
            validationResults.add(optName.nameString, "missing required parameter value")
            emptyList()
        } else {
            doCheckLangCodes(identificationLabelLanguages, optName, validationResults)
        }
    }

    fun checkTranslationLanguages(
        translationLanguages: String?,
        optName: OptName,
        validationResults: OptionValidationResults
    ): List<String>? {

        return if (translationLanguages == null) {
            null
        } else {
            doCheckLangCodes(translationLanguages, optName, validationResults)
        }
    }

    private fun doCheckLangCodes(
        langCodes: String,
        optName: OptName,
        validationResults: OptionValidationResults
    ): List<String> {
        val codes = langCodes.split(",").map { it.trim() }

        if (codes.isEmpty()) {
            validationResults.add(optName.nameString, "is empty")
        }

        if (codes.any { language -> language.isBlank() }) {
            validationResults.add(optName.nameString, "has blank language code")
        }

        if (codes.any { language -> language.any { !it.isLetter() } }) {
            validationResults.add(optName.nameString, "language code has illegal characters")
        }

        if (codes
                .groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
                .isNotEmpty()
        ) {
            validationResults.add(optName.nameString, "has duplicate language codes")
        }

        return codes
    }
}
