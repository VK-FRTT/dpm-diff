package fi.vm.dpm.diff.cli.unit

import fi.vm.dpm.diff.cli.DiffCliExceptionHarness
import fi.vm.dpm.diff.cli.PrintStreamCollector
import fi.vm.dpm.diff.model.FailException
import fi.vm.dpm.diff.model.HaltException
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DiffCliExceptionHarnessTest {
    private lateinit var charset: Charset
    private lateinit var printStreamCollector: PrintStreamCollector

    @BeforeEach
    fun testInit() {
        charset = StandardCharsets.UTF_8
        printStreamCollector = PrintStreamCollector(charset)
    }

    @Test
    fun `when action succeeds, ExceptionHarness should return 0`() {

        val writer = PrintWriter(BufferedWriter(OutputStreamWriter(printStreamCollector.printStream(), charset)), true)

        val result = DiffCliExceptionHarness.withExceptionHarness(writer) {
        }

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `when action throws HaltException, ExceptionHarness should return 0`() {

        val writer = PrintWriter(BufferedWriter(OutputStreamWriter(printStreamCollector.printStream(), charset)), true)

        val result = DiffCliExceptionHarness.withExceptionHarness(writer) {
            throw HaltException()
        }

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `when action throws FailException, ExceptionHarness should return 1 and output exception message`() {

        val writer = PrintWriter(BufferedWriter(OutputStreamWriter(printStreamCollector.printStream(), charset)), true)

        val result = DiffCliExceptionHarness.withExceptionHarness(writer) {
            throw FailException("FailException from test")
        }

        assertThat(result).isEqualTo(1)
        assertThat(printStreamCollector.grabText()).contains("FailException from test")
    }

    @Test
    fun `when action throws some other exception, ExceptionHarness should return 1 and output exception message with stack trace`() {

        val writer = PrintWriter(BufferedWriter(OutputStreamWriter(printStreamCollector.printStream(), charset)), true)

        val result = DiffCliExceptionHarness.withExceptionHarness(writer) {
            throw UnsupportedOperationException("UnsupportedOperationException from test")
        }

        assertThat(result).isEqualTo(1)
        assertThat(printStreamCollector.grabText()).containsSubsequence(
            "DPM Diff",
            "java.lang.UnsupportedOperationException: UnsupportedOperationException from test",
            "at fi.vm.dpm.diff.cli.unit.DiffCliExceptionHarnessTest$"
        )
    }
}
