package fi.vm.dpm.diff.cli

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private val JAR_PATH_PATTERN =
    """
        \A
        jar:file:
        (?<jarPath>[^!]+)
        !.+
        \z
    """.trimIndent().toRegex(RegexOption.COMMENTS)

data class DetectedOptions(
    val cmdShowHelp: Boolean,
    val cmdShowVersion: Boolean,
    val baselineDpmDb: Path?,
    val changedDpmDb: Path?,
    val reportConfig: Path?,
    val verbosity: OutputVerbosity
) {
    fun validDpmDiffCmdParams(): DiffCmdParams {
        val validationResults = OptionValidationResults()

        val params = DiffCmdParams(
            baselineDpmDb = pathToExistingFile(baselineDpmDb, OptName.BASELINE_DPM_DB, validationResults),
            changedDpmDb = pathToExistingFile(changedDpmDb, OptName.CHANGED_DPM_DB, validationResults),
            reportConfig = resolveReportConfigPath(validationResults)
        )

        validationResults.failOnErrors()

        return params
    }

    private fun pathToExistingFile(
        filePath: Path?,
        optName: OptName,
        validationResults: OptionValidationResults
    ): Path {
        return if (filePath == null) {
            filePath ?: validationResults.add(optName.nameString, "missing required parameter value")
            Paths.get("")
        } else {
            if (!Files.exists(filePath)) {
                validationResults.add(optName.nameString, "file not found ($filePath)")
            }
            filePath
        }
    }

    private fun resolveReportConfigPath(validationResults: OptionValidationResults): Path {
        return if (reportConfig != null) {
            pathToExistingFile(
                reportConfig,
                OptName.REPORT_CONFIG,
                validationResults
            )
        } else {
            pathToDefaultFile(
                "dpm-diff-report-config.json",
                OptName.REPORT_CONFIG,
                validationResults
            )
        }
    }

    private fun pathToDefaultFile(
        fileName: String,
        optName: OptName,
        validationResults: OptionValidationResults
    ): Path {
        val curDirConfig = pathToCurrentDirDefaultFile(fileName)

        if (Files.exists(curDirConfig)) {
            return curDirConfig
        }

        val jarDirConfig = pathToJavaDirDefaultFile(fileName)

        if (jarDirConfig != null && Files.exists(jarDirConfig)) {
            return jarDirConfig
        }

        validationResults.add(
            "${optName.nameString} default",
            "file not found",
            listOfNotNull(curDirConfig, jarDirConfig)
        )

        return Paths.get("")
    }

    private fun pathToCurrentDirDefaultFile(fileName: String): Path {
        val currentDir = Paths.get("")
        return currentDir.resolve(fileName).toAbsolutePath()
    }

    private fun pathToJavaDirDefaultFile(fileName: String): Path? {
        val selfResourceName = "/${javaClass.name.replace('.', '/')}.class"
        val selfResourceUrl = javaClass.getResource(selfResourceName)

        val jarPathMatch = JAR_PATH_PATTERN.matchEntire(selfResourceUrl.toString())
        jarPathMatch ?: return null

        val rawJarPath = (jarPathMatch.groups as MatchNamedGroupCollection)["jarPath"]?.value
        rawJarPath ?: return null

        val normalizedJarPath = File(rawJarPath).toPath().toAbsolutePath()

        val jarDirPath = normalizedJarPath.parent

        return jarDirPath.resolve(fileName)
    }
}
