package fi.vm.dpm.diff.repgen.dpm

data class DpmSectionOptions(
    val identificationLabelLangCodes: List<String>,
    val translationLangCodes: List<String>?
) {
    fun toReportGenerationOptions() = listOf(
        "IdentificationLabelLanguages: ${identificationLabelLangCodes.joinToString()}",
        "TranslationLanguages: ${translationLangCodes?.joinToString() ?: "ALL"}"
    )
}
