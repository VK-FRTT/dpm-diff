package fi.vm.dpm.diff.cli

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object PathOptions {

    fun checkExistingFile(
        filePath: Path?,
        optName: OptName,
        validationResults: OptionValidationResults
    ): Path {
        val path = if (filePath == null) {
            validationResults.add(optName.nameString, "missing required parameter value")
            Paths.get("")
        } else {
            if (!Files.exists(filePath)) {
                validationResults.add(optName.nameString, "file not found ($filePath)")
            }
            filePath
        }

        return path.toAbsolutePath().normalize()
    }

    fun checkWritableFile(
        filePath: Path?,
        forceOverwrite: Boolean,
        optName: OptName,
        validationResults: OptionValidationResults
    ): Path {
        val path = if (filePath == null) {
            filePath ?: validationResults.add(optName.nameString, "missing required parameter value")
            Paths.get("")
        } else {
            val normalizedPath = filePath.toAbsolutePath().normalize()

            if (forceOverwrite && Files.isRegularFile(filePath)) {
                try {
                    Files.delete(normalizedPath)
                } catch (ex: Exception) {
                    validationResults.add(optName.nameString, "removing existing file failed: ${ex.message}")
                }
            }

            if (Files.exists(normalizedPath)) {
                validationResults.add(optName.nameString, "file '$normalizedPath' already exists")
            }

            Files.createDirectories(normalizedPath.parent)

            normalizedPath
        }

        return path
    }
}
