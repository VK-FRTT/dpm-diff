package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
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

    fun compareDpmOptions(
        diagnostic: Diagnostic
    ): Pair<CommonCompareOptions, DpmSectionOptions> {

        val validationResults = OptionValidationResults()

        val options = Pair(
            commonCompareOptions(validationResults),
            dpmSectionOptions(validationResults)
        )

        validationResults.reportErrors(diagnostic)

        return options
    }

    fun compareVkDataOptions(
        diagnostic: Diagnostic
    ): CommonCompareOptions {

        val validationResults = OptionValidationResults()

        val options = commonCompareOptions(validationResults)

        validationResults.reportErrors(diagnostic)

        return options
    }

    private fun commonCompareOptions(
        validationResults: OptionValidationResults
    ): CommonCompareOptions {

        return CommonCompareOptions(
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
            )
        )
    }

    private fun dpmSectionOptions(
        validationResults: OptionValidationResults
    ): DpmSectionOptions {

        return DpmSectionOptions(
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
    }
}
