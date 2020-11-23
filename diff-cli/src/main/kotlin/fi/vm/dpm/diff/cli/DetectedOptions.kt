package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,
    val cmdShowVersion: Boolean,
    val baselineDpmDbPath: Path?,
    val currentDpmDbPath: Path?,
    val outputFilePath: Path?,
    val forceOverwrite: Boolean,
    val identificationLabelLanguages: String?,
    val translationLanguages: String?,
    val verbosity: OutputVerbosity
) {
    fun dpmDiffReportParams(diagnostic: Diagnostic): DpmDiffReportParams {
        val validationResults = OptionValidationResults()

        val params = DpmDiffReportParams(
            baselineDpmDbPath = PathOptions.checkExistingFile(
                baselineDpmDbPath,
                OptName.BASELINE_DPM_DB,
                validationResults
            ),

            currentDpmDbPath = PathOptions.checkExistingFile(
                currentDpmDbPath,
                OptName.CURRENT_DPM_DB,
                validationResults
            ),

            outputFilePath = PathOptions.checkWritableFile(
                outputFilePath,
                forceOverwrite,
                OptName.OUTPUT,
                validationResults
            ),

            forceOverwrite = forceOverwrite,

            identificationLabelLangCodes = LangCodeOptions.checkIdentificationLabelLanguages(
                identificationLabelLanguages,
                OptName.IDENTIFICATION_LABEL_LANGUAGES,
                validationResults
            ),

            translationLangCodes = LangCodeOptions.checkTranslationLanguages(
                translationLanguages,
                OptName.TRANSLATION_LANGUAGES,
                validationResults
            )
        )

        validationResults.reportErrors(diagnostic)

        return params
    }
}
