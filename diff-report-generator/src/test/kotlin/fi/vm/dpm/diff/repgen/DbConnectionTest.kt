package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.SourceKind
import java.nio.file.Files
import java.nio.file.Path
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DbConnectionTest {

    private lateinit var tempFolderPath: Path
    private lateinit var dbPath: Path
    private lateinit var diagnosticCollector: DiagnosticCollector

    @BeforeEach
    fun setup() {
        tempFolderPath = Files.createTempDirectory("db_connection_test")
        dbPath = tempFolderPath.resolve("test.db")
        diagnosticCollector = DiagnosticCollector()
    }

    @AfterEach
    fun teardown() {
        Files
            .walk(tempFolderPath)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    @Test
    fun `Should emit diagnostic messages on query failure`() {

        val connection = DbConnection(
            dbPath,
            "sqlite",
            SourceKind.BASELINE,
            diagnosticCollector
        )

        val thrown = catchThrowable {
            connection.executeQuery("Query", "QueryDebugName") {
            }
        }

        assertThat(thrown).isInstanceOf(ArithmeticException::class.java)

        assertThat(diagnosticCollector.messages[0]).startsWith("DEBUG: Query QueryDebugName BASELINE:[")
        assertThat(diagnosticCollector.messages[1]).startsWith("DEBUG: DB query failure:")
        assertThat(diagnosticCollector.messages[2]).startsWith("FATAL: DB query failure: [SQLITE_ERROR] SQL error or missing database")
    }
}
