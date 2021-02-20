package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.ValidationResults

object ValidateAndTransformLangCodeOption {

    fun identificationLabelLanguages(
        identificationLabelLanguages: String?,
        validationResults: ValidationResults
    ): List<String> {
        val optName = OptName.IDENTIFICATION_LABEL_LANGUAGES

        return if (identificationLabelLanguages == null) {
            validationResults.add(optName.nameString, "missing required parameter value")
            emptyList()
        } else {
            doCheckLangCodes(identificationLabelLanguages, optName, validationResults) ?: emptyList()
        }
    }

    fun translationLanguages(
        translationLanguages: String?,
        validationResults: ValidationResults
    ): List<String>? {
        val optName = OptName.TRANSLATION_LANGUAGES

        return if (translationLanguages == null) {
            null
        } else {
            doCheckLangCodes(translationLanguages, optName, validationResults)
        }
    }

    private fun doCheckLangCodes(
        langCodes: String,
        optName: OptName,
        validationResults: ValidationResults
    ): List<String>? {
        if (langCodes.isEmpty()) {
            validationResults.add(optName.nameString, "is empty")
            return null
        }

        val codes = langCodes.split(",").map { it.trim() }

        return when {

            codes.any { it.isBlank() } -> {
                validationResults.add(optName.nameString, "has blank language code")
                null
            }
            codes.any { language -> language.any { !it.isLetter() } } -> {
                validationResults.add(optName.nameString, "language code has illegal characters")
                null
            }

            codes.groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
                .isNotEmpty() -> {
                validationResults.add(optName.nameString, "has duplicate language codes")
                null
            }
            else -> {
                codes
            }
        }
    }
}
