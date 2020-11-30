package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,
    val cmdShowVersion: Boolean,
    val cmdCompareDpm: Boolean,
    val cmdCompareVkData: Boolean,

    val baselineDpmDbPath: Path?,
    val currentDpmDbPath: Path?,
    val outputFilePath: Path?,
    val forceOverwrite: Boolean,
    val verbosity: OutputVerbosity,

    val identificationLabelLanguages: String?,
    val translationLanguages: String?
) {

    fun ensureSingleCommandGiven(diagnostic: Diagnostic) {
        val commandCount = listOf(
            cmdShowHelp,
            cmdShowVersion,
            cmdCompareDpm,
            cmdCompareVkData
        ).count { it }

        if (commandCount != 1) {
            diagnostic.fatal("Single command must be given")
        }
    }

    fun compareParamsDpm(diagnostic: Diagnostic): CompareParamsDpm {
        val validationResults = OptionValidationResults()

        val params = CompareParamsDpm(

            common = compareParamsCommon(validationResults),

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

    fun compareParamsVkData(diagnostic: Diagnostic): CompareParamsVkData {
        val validationResults = OptionValidationResults()

        val params = CompareParamsVkData(
            common = compareParamsCommon(validationResults)
        )

        validationResults.reportErrors(diagnostic)

        return params
    }

    private fun compareParamsCommon(validationResults: OptionValidationResults): CompareParamsCommon {
        return CompareParamsCommon(
            baselineDbPath = PathOptions.checkExistingFile(
                baselineDpmDbPath,
                OptName.BASELINE_DB,
                validationResults
            ),

            currentDbPath = PathOptions.checkExistingFile(
                currentDpmDbPath,
                OptName.CURRENT_DB,
                validationResults
            ),

            outputFilePath = PathOptions.checkWritableFile(
                outputFilePath,
                forceOverwrite,
                OptName.OUTPUT,
                validationResults
            ),

            forceOverwrite = forceOverwrite
        )
    }
}
