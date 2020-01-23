package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,
    val cmdShowVersion: Boolean,
    val baselineDpmDbPath: Path?,
    val actualDpmDbPath: Path?,
    val reportConfigPath: Path?,
    val outputFilePath: Path?,
    val forceOverwrite: Boolean,
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

            actualDpmDbPath = PathOptions.checkExistingFile(
                actualDpmDbPath,
                OptName.ACTUAL_DPM_DB,
                validationResults
            ),

            reportConfig = PathOptions.checkExistingFileOrDefaultFallback(
                reportConfigPath,
                "dpm-diff-report-config.json",
                OptName.REPORT_CONFIG,
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

        validationResults.reportErrors(diagnostic)

        return params
    }
}
