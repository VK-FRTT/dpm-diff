package fi.vm.dpm.diff.cli

import java.io.File
import java.lang.Exception
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
            filePath ?: validationResults.add(optName.nameString, "missing required parameter value")
            Paths.get("")
        } else {
            if (!Files.exists(filePath)) {
                validationResults.add(optName.nameString, "file not found ($filePath)")
            }
            filePath
        }

        return path.toAbsolutePath().normalize()
    }

    fun checkExistingFileOrDefaultFallback(
        reportConfigPath: Path?,
        defaultFileName: String,
        optName: OptName,
        validationResults: OptionValidationResults
    ): Path {
        val path = if (reportConfigPath != null) {
            checkExistingFile(
                reportConfigPath,
                optName,
                validationResults
            )
        } else {
            pathToDefaultFile(
                defaultFileName,
                optName,
                validationResults
            )
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

    private fun pathToDefaultFile(
        defaultFileName: String,
        optName: OptName,
        validationResults: OptionValidationResults
    ): Path {
        val curDirConfig = pathToCurrentDirDefaultFile(defaultFileName)

        if (Files.exists(curDirConfig)) {
            return curDirConfig
        }

        val jarDirConfig = pathToJavaDirDefaultFile(defaultFileName)

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
        return currentDir.resolve(fileName)
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

    private val JAR_PATH_PATTERN =
        """
        \A
        jar:file:
        (?<jarPath>[^!]+)
        !.+
        \z
    """.trimIndent().toRegex(RegexOption.COMMENTS)
}
