package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.ChangeReportKind
import fi.vm.dpm.diff.model.DpmSectionPlans
import fi.vm.dpm.diff.model.FailException
import fi.vm.dpm.diff.model.HaltException
import fi.vm.dpm.diff.model.ReportGeneratorDescriptor
import fi.vm.dpm.diff.model.SpreadsheetOutput
import fi.vm.dpm.diff.model.VkDataSectionPlans
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.model.throwHalt
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.SourceDbs
import fi.vm.dpm.diff.repgen.SqlReportGenerator
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
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

            detectedOptions.ensureSingleCommandGiven(diagnostic)

            if (detectedOptions.cmdShowHelp) {
                definedOptions.printHelp(outWriter)
                throwHalt()
            }

            if (detectedOptions.cmdShowVersion) {
                DiffCliVersion.printVersion(outWriter)
                throwHalt()
            }

            if (detectedOptions.cmdCompareDpm) {
                val (commonOptions, dpmSectionOptions) = detectedOptions.compareDpmOptions(diagnostic)
                compareDpm(commonOptions, dpmSectionOptions, diagnostic)
                throwHalt()
            }

            if (detectedOptions.cmdCompareVkData) {
                val commonOptions = detectedOptions.compareVkDataOptions(diagnostic)
                compareVkData(commonOptions, diagnostic)
                throwHalt()
            }
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

    private fun compareDpm(
        commonOptions: CommonCompareOptions,
        dpmSectionOptions: DpmSectionOptions,
        diagnostic: Diagnostic
    ) {
        val sourceDbs = SourceDbs(
            baselineDbPath = commonOptions.baselineDbPath,
            currentDbPath = commonOptions.currentDbPath,
            jdbcDriver = "sqlite",
            diagnostic = diagnostic
        )

        sourceDbs.use {
            val sectionPlans = filterIncludedReportSections(
                sectionPlans = DpmSectionPlans.allPlans(dpmSectionOptions),
                commonOptions = commonOptions
            )

            generateAndRenderSqlBasedReport(
                reportKind = ChangeReportKind.DPM,
                sectionPlans = sectionPlans,
                sourceDbs = sourceDbs,
                reportGeneratorDescriptor = reportGeneratorDescriptor(),
                reportGenerationOptions = dpmSectionOptions.toReportGenerationOptions(),
                commonOptions = commonOptions,
                diagnostic = diagnostic
            )
        }
    }

    private fun compareVkData(
        commonOptions: CommonCompareOptions,
        diagnostic: Diagnostic
    ) {
        val sourceDbs = SourceDbs(
            baselineDbPath = commonOptions.baselineDbPath,
            currentDbPath = commonOptions.currentDbPath,
            jdbcDriver = "sqlite",
            diagnostic = diagnostic
        )

        sourceDbs.use {
            val sectionPlans = filterIncludedReportSections(
                sectionPlans = VkDataSectionPlans.allPlans(sourceDbs),
                commonOptions = commonOptions
            )

            generateAndRenderSqlBasedReport(
                reportKind = ChangeReportKind.VK_DATA,
                sectionPlans = sectionPlans,
                sourceDbs = sourceDbs,
                reportGeneratorDescriptor = reportGeneratorDescriptor(),
                reportGenerationOptions = emptyList(),
                commonOptions = commonOptions,
                diagnostic = diagnostic
            )
        }
    }

    private fun reportGeneratorDescriptor(): ReportGeneratorDescriptor {
        val version = DiffCliVersion.resolveVersion()

        return ReportGeneratorDescriptor(
            title = DPM_DIFF_TITLE,
            revision = version.buildRevision,
            originUrl = version.originUrl
        )
    }

    private fun filterIncludedReportSections(
        sectionPlans: Collection<SectionPlanSql>,
        commonOptions: CommonCompareOptions
    ): Collection<SectionPlanSql> {
        if (commonOptions.reportSections == null) return sectionPlans

        return sectionPlans.filter { sectionPlan ->
            commonOptions.reportSections.any { includedSectionName ->
                includedSectionName.equals(
                    other = sectionPlan.sectionOutline.sectionShortTitle,
                    ignoreCase = true
                )
            }
        }
    }

    private fun generateAndRenderSqlBasedReport(
        reportKind: ChangeReportKind,
        sectionPlans: Collection<SectionPlanSql>,
        sourceDbs: SourceDbs,
        reportGeneratorDescriptor: ReportGeneratorDescriptor,
        reportGenerationOptions: List<String>,
        commonOptions: CommonCompareOptions,
        diagnostic: Diagnostic
    ) {
        val generator = SqlReportGenerator(
            reportKind = reportKind,
            sectionPlans = sectionPlans,
            sourceDbs = sourceDbs,
            reportGeneratorDescriptor = reportGeneratorDescriptor,
            reportGenerationOptions = reportGenerationOptions,
            diagnostic = diagnostic
        )

        val report = generator.generateReport()

        SpreadsheetOutput(
            outputFilePath = commonOptions.outputFilePath,
            diagnostic = diagnostic
        ).use { output ->
            output.renderOutput(report)
        }
    }
}
