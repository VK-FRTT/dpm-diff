package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.ValidationResults
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object ValidateAndTransformPathOption {

    private val emptyPath = Paths.get("")

    fun existingFile(
        filePath: Path?,
        optName: OptName,
        validationResults: ValidationResults
    ): Path {

        return if (filePath == null) {
            validationResults.add(optName.nameString, "missing required parameter value")
            emptyPath
        } else {
            val normalizedPath = filePath.toAbsolutePath().normalize()

            if (!Files.exists(normalizedPath)) {
                validationResults.add(optName.nameString, "file not found ($normalizedPath)")
                emptyPath
            } else {
                normalizedPath
            }
        }
    }

    fun writableFile(
        filePath: Path?,
        forceOverwrite: Boolean,
        optName: OptName,
        validationResults: ValidationResults
    ): Path {
        return if (filePath == null) {
            validationResults.add(optName.nameString, "missing required parameter value")
            emptyPath
        } else {
            val normalizedPath = filePath.toAbsolutePath().normalize()

            val resultPath = if (Files.exists(normalizedPath)) {

                if (forceOverwrite) {
                    try {
                        Files.delete(normalizedPath)
                        normalizedPath
                    } catch (ex: Exception) {
                        validationResults.add(optName.nameString, "removing existing file failed: ${ex.message}")
                        emptyPath
                    }
                } else {
                    validationResults.add(optName.nameString, "file ($normalizedPath) already exists")
                    emptyPath
                }
            } else {
                normalizedPath
            }

            if (resultPath != emptyPath) {
                Files.createDirectories(resultPath.parent)
            }

            resultPath
        }
    }
}
