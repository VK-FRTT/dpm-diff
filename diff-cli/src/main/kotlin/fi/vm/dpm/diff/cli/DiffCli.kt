package fi.vm.dpm.diff.cli

import java.io.BufferedWriter
import java.io.Closeable
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter
import java.nio.charset.Charset
import java.sql.DriverManager

const val DPM_DIFF_CLI_SUCCESS = 0
const val DPM_DIFF_CLI_FAIL = 1
const val DPM_DIFF_TITLE = "DPM Diff"

internal class DiffCli(
    outStream: PrintStream,
    errStream: PrintStream,
    charset: Charset,
    private val definedOptions: DefinedOptions
) : Closeable {

    private val outWriter = PrintWriter(BufferedWriter(OutputStreamWriter(outStream, charset)), true)
    private val errWriter = PrintWriter(BufferedWriter(OutputStreamWriter(errStream, charset)), true)

    override fun close() {
        outWriter.close()
        errWriter.close()
    }

    fun execute(args: Array<String>): Int {
        return withExceptionHarness {
            outWriter.println(DPM_DIFF_TITLE)

            val detectedOptions = definedOptions.detectOptionsFromArgs(args)

            if (detectedOptions.cmdShowHelp) {
                definedOptions.printHelp(outWriter)
                throwHalt()
            }

            if (detectedOptions.cmdShowVersion) {
                DiffCliVersion.printVersion(outWriter)
                throwHalt()
            }

            runDpmDiff(detectedOptions.validDpmDiffCmdParams())
        }
    }

    private fun withExceptionHarness(steps: () -> Unit): Int {
        return try {
            steps()
            DPM_DIFF_CLI_SUCCESS
        } catch (exception: HaltException) {
            DPM_DIFF_CLI_SUCCESS
        } catch (exception: FailException) {
            errWriter.println("\n${exception.message}")
            errWriter.println()

            DPM_DIFF_CLI_FAIL
        } catch (exception: Throwable) {
            errWriter.println(DPM_DIFF_TITLE)
            exception.printStackTrace(errWriter)
            errWriter.println()

            DPM_DIFF_CLI_FAIL
        }
    }

    private fun runDpmDiff(diffParams: DiffCmdParams) {
        val baselineConnection = DriverManager.getConnection("jdbc:sqlite:${diffParams.baselineDpmDb}")
        val changedConnection = DriverManager.getConnection("jdbc:sqlite:${diffParams.changedDpmDb}")
    }
}
