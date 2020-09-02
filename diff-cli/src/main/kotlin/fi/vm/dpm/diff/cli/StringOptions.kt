package fi.vm.dpm.diff.cli

object StringOptions {

    fun checkIdentificationLabelLanguages(
        identificationLabelLanguages: String?,
        optName: OptName,
        validationResults: OptionValidationResults
    ): List<String> {

        return if (identificationLabelLanguages == null) {
            validationResults.add(optName.nameString, "missing required parameter value")
            emptyList()
        } else {
            val langCodes = identificationLabelLanguages.split(",").map { it.trim() }

            if (langCodes.isEmpty()) {
                validationResults.add(optName.nameString, "is empty")
            }

            if (langCodes.any { language -> language.isBlank() }) {
                validationResults.add(optName.nameString, "has blank language code")
            }

            if (langCodes.any { language -> language.any { !it.isLetter() } }) {
                validationResults.add(optName.nameString, "language code has illegal characters")
            }

            if (langCodes
                    .groupingBy { it }
                    .eachCount()
                    .filter { it.value > 1 }
                    .isNotEmpty()
            ) {
                validationResults.add(optName.nameString, "has duplicate language codes")
            }

            langCodes
        }
    }
}
