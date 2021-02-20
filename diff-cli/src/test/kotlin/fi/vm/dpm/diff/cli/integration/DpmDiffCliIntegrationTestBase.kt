package fi.vm.dpm.diff.cli.integration

import fi.vm.dpm.diff.cli.DPM_DIFF_CLI_FAIL
import fi.vm.dpm.diff.cli.DPM_DIFF_CLI_SUCCESS
import fi.vm.dpm.diff.cli.DiffCli
import fi.vm.dpm.diff.cli.PrintStreamCollector
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class DpmDiffCliIntegrationTestBase {
    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: DiffCli

    @BeforeEach
    fun testBaseInit() {
        charset = StandardCharsets.UTF_8
        outCollector = PrintStreamCollector(charset)
        errCollector = PrintStreamCollector(charset)

        cli = DiffCli(
            outStream = outCollector.printStream(),
            errStream = errCollector.printStream(),
            charset = charset
        )
    }

    @AfterEach
    fun testBaseTeardown() {
        cli.close()
    }

    protected fun executeCliAndExpectSuccess(args: Array<String>, verifyAction: (String) -> Unit) {
        val result = executeCli(args)

        assertThat(result.errText).isBlank()

        verifyAction(result.outText)

        assertThat(result.status).isEqualTo(DPM_DIFF_CLI_SUCCESS)
    }

    protected fun executeCliAndExpectFail(args: Array<String>, verifier: (String, String) -> Unit) {
        val result = executeCli(args)

        assertThat(result.errText).isNotBlank()

        verifier(result.outText, result.errText)

        assertThat(result.status).isEqualTo(DPM_DIFF_CLI_FAIL)
    }

    private fun executeCli(args: Array<String>): ExecuteResult {
        outCollector.printStream().write("CLI args:\n ${args.joinToString(separator = "\n")}\n\n\n".toByteArray())

        val status = cli.execute(args)

        val result = ExecuteResult(
            status,
            outCollector.grabText(),
            errCollector.grabText()
        )

        return result
    }

    private data class ExecuteResult(
        val status: Int,
        val outText: String,
        val errText: String
    )
}
