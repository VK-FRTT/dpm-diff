package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.DpmDiffReportGenerator
import fi.vm.dpm.diff.model.FailException
import fi.vm.dpm.diff.model.HaltException
import fi.vm.dpm.diff.model.ReportGeneratorDescriptor
import fi.vm.dpm.diff.model.SpreadsheetOutput
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.model.throwHalt
import java.io.BufferedWriter
import java.io.Closeable
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter
import java.nio.charset.Charset

const val DPM_DIFF_CLI_SUCCESS = 0
const val DPM_DIFF_CLI_FAIL = 1
const val DPM_DIFF_TITLE = "DPM Diff"

internal class DiffCli(
    outStream: PrintStream,
    errStream: PrintStream,
    charset: Charset
) : Closeable {

    private val definedOptions = DefinedOptions()
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

            val diagnostic = DiffCliDiagnostic(outWriter, detectedOptions.verbosity)

            if (detectedOptions.cmdShowHelp) {
                definedOptions.printHelp(outWriter)
                throwHalt()
            }

            if (detectedOptions.cmdShowVersion) {
                DiffCliVersion.printVersion(outWriter)
                throwHalt()
            }

            val dpmDiffReportParams = detectedOptions.dpmDiffReportParams(diagnostic)
            executeDpmDiffReport(dpmDiffReportParams, diagnostic)
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

    private fun executeDpmDiffReport(
        diffParams: DpmDiffReportParams,
        diagnostic: Diagnostic
    ) {
        val version = DiffCliVersion.resolveVersion()

        val reportGeneratorDescriptor = ReportGeneratorDescriptor(
            title = DPM_DIFF_TITLE,
            revision = version.buildRevision,
            originUrl = version.originUrl
        )

        val report = DpmDiffReportGenerator(
            baselineDpmDbPath = diffParams.baselineDpmDbPath,
            currentDpmDbPath = diffParams.currentDpmDbPath,
            reportGeneratorDescriptor = reportGeneratorDescriptor,
            identificationLabelLangCodes = diffParams.identificationLabelLangCodes,
            translationLangCodes = diffParams.translationLangCodes,
            diagnostic = diagnostic
        ).use { generator ->
            generator.generateReport()
        }

        SpreadsheetOutput(
            outputFilePath = diffParams.outputFilePath,
            diagnostic = diagnostic
        ).use { output ->
            output.renderOutput(report)
        }
    }
}
