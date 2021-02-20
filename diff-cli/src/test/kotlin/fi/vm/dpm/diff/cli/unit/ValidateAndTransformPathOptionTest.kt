package fi.vm.dpm.diff.cli.unit

import fi.vm.dpm.diff.cli.OptName
import fi.vm.dpm.diff.cli.ValidateAndTransformPathOption
import fi.vm.dpm.diff.cli.integration.TempFolder
import fi.vm.dpm.diff.model.diagnostic.ValidationResults
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.Permission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ValidateAndTransformPathOptionTest {

    private lateinit var tempFolder: TempFolder
    private lateinit var emptyPath: Path
    private lateinit var existingFilePath: Path
    private lateinit var validationResults: ValidationResults

    @BeforeEach
    fun testInit() {
        tempFolder = TempFolder("path_options_test")
        emptyPath = Paths.get("")
        existingFilePath = Files.createTempFile(tempFolder.rootPath, "existing_file", "tmp")
        validationResults = ValidationResults()
    }

    @AfterEach
    fun testTeardown() {
        tempFolder.close()
    }

    @Nested
    inner class ExistingFile {

        @Test
        fun `Should provide validation message when input is null`() {
            val path = ValidateAndTransformPathOption.existingFile(
                filePath = null,
                optName = OptName.BASELINE_DB,
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "baselineDb: missing required parameter value"
            )

            assertThat(path).isEqualTo(emptyPath)
        }

        @Test
        fun `Should provide validation message when input points to non-existing file`() {
            val path = ValidateAndTransformPathOption.existingFile(
                filePath = Paths.get("/missing_folder/file.tmp"),
                optName = OptName.BASELINE_DB,
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "baselineDb: file not found (/missing_folder/file.tmp)"
            )

            assertThat(path).isEqualTo(emptyPath)
        }

        @Test
        fun `Should provide normalized path in validation message when input points to non-existing file`() {
            val path = ValidateAndTransformPathOption.existingFile(
                filePath = Paths.get("/missing_folder/sub1/sub2/../../file.tmp"),
                optName = OptName.BASELINE_DB,
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "baselineDb: file not found (/missing_folder/file.tmp)"
            )

            assertThat(path).isEqualTo(emptyPath)
        }

        @Test
        fun `Should not provide validation message when input points to existing file`() {

            val existingFilePath = Files.createTempFile(
                tempFolder.rootPath,
                "existing_file",
                "tmp"
            )

            val path = ValidateAndTransformPathOption.existingFile(
                filePath = existingFilePath,
                optName = OptName.BASELINE_DB,
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).isEmpty()
            assertThat(Files.exists(path)).isTrue()
            assertThat(path).isEqualTo(existingFilePath)
        }
    }

    @Nested
    inner class WritableFile {

        @Test
        fun `Should provide validation message when input is null`() {
            val path = ValidateAndTransformPathOption.writableFile(
                filePath = null,
                forceOverwrite = false,
                optName = OptName.OUTPUT,
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "output: missing required parameter value"
            )

            assertThat(path).isEqualTo(emptyPath)
        }
    }

    @Test
    fun `Should provide validation message when input points to already existing file`() {
        val path = ValidateAndTransformPathOption.writableFile(
            filePath = existingFilePath,
            forceOverwrite = false,
            optName = OptName.OUTPUT,
            validationResults = validationResults
        )

        assertThat(validationResults.messages()).containsExactly(
            "output: file ($existingFilePath) already exists"
        )
        assertThat(path).isEqualTo(emptyPath)
        assertThat(Files.exists(existingFilePath)).isTrue()
    }

    @Test
    fun `Should not provide validation message when forceOverwrite allows deleting already existing file`() {
        val path = ValidateAndTransformPathOption.writableFile(
            filePath = existingFilePath,
            forceOverwrite = true,
            optName = OptName.OUTPUT,
            validationResults = validationResults
        )

        assertThat(validationResults.messages()).isEmpty()
        assertThat(path).isEqualTo(existingFilePath)
        assertThat(Files.exists(path)).isFalse()
    }

    @Test
    fun `Should provide validation message when forceOverwrite file deletion fails`() {
        System.setSecurityManager(object : SecurityManager() {

            override fun checkDelete(file: String) {
                throw SecurityException("Reason from test")
            }

            override fun checkPermission(perm: Permission) {
                return
            }
        })

        val path = ValidateAndTransformPathOption.writableFile(
            filePath = existingFilePath,
            forceOverwrite = true,
            optName = OptName.OUTPUT,
            validationResults = validationResults
        )

        System.setSecurityManager(null)

        assertThat(validationResults.messages()).containsExactly(
            "output: removing existing file failed: Reason from test"
        )
        assertThat(path).isEqualTo(emptyPath)
        assertThat(Files.exists(existingFilePath)).isTrue()
    }

    @Test
    fun `Should not provide validation message but create parent folders when input points to non existing file`() {
        val path = ValidateAndTransformPathOption.writableFile(
            filePath = tempFolder.resolve("sub1/../sub2/sub3/output.db"),
            forceOverwrite = true,
            optName = OptName.OUTPUT,
            validationResults = validationResults
        )

        assertThat(validationResults.messages()).isEmpty()
        assertThat(path).isEqualTo(tempFolder.resolve("sub2/sub3/output.db"))
        assertThat(Files.exists(path.parent)).isTrue()
        assertThat(Files.exists(path)).isFalse()
    }
}
