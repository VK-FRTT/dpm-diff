package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.model.diagnostic.ValidationResults
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,
    val cmdShowVersion: Boolean,
    val cmdCompareDpm: Boolean,
    val cmdCompareVkData: Boolean,

    private val baselineDpmDbPath: Path?,
    private val currentDpmDbPath: Path?,
    private val outputFilePath: Path?,
    private val forceOverwrite: Boolean,
    val verbosity: OutputVerbosity,
    private val reportSections: String?,

    private val identificationLabelLanguages: String?,
    private val translationLanguages: String?
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

        val validationResults = ValidationResults()

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

        val validationResults = ValidationResults()

        val options = commonCompareOptions(validationResults)

        validationResults.reportErrors(diagnostic)

        return options
    }

    private fun commonCompareOptions(
        validationResults: ValidationResults
    ): CommonCompareOptions {

        return CommonCompareOptions(
            baselineDbPath = ValidateAndTransformPathOption.existingFile(
                baselineDpmDbPath,
                OptName.BASELINE_DB,
                validationResults
            ),

            currentDbPath = ValidateAndTransformPathOption.existingFile(
                currentDpmDbPath,
                OptName.CURRENT_DB,
                validationResults
            ),

            outputFilePath = ValidateAndTransformPathOption.writableFile(
                outputFilePath,
                forceOverwrite,
                OptName.OUTPUT,
                validationResults
            ),

            reportSections = ValidateAndTransformReportSectionOption.includedReportSections(
                reportSections,
                validationResults
            )
        )
    }

    private fun dpmSectionOptions(
        validationResults: ValidationResults
    ): DpmSectionOptions {

        return DpmSectionOptions(
            identificationLabelLangCodes = ValidateAndTransformLangCodeOption.identificationLabelLanguages(
                identificationLabelLanguages,
                validationResults
            ),

            translationLangCodes = ValidateAndTransformLangCodeOption.translationLanguages(
                translationLanguages,
                validationResults
            )
        )
    }
}
