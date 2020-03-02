package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.throwFail
import java.io.PrintWriter
import java.nio.file.Path
import java.util.LinkedHashSet
import joptsimple.BuiltinHelpFormatter
import joptsimple.OptionDescriptor
import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSpec
import joptsimple.ValueConversionException
import joptsimple.util.EnumConverter
import joptsimple.util.PathConverter

enum class OptName(val nameString: String) {
    BASELINE_DPM_DB("baseline-dpm-db"),
    CURRENT_DPM_DB("current-dpm-db"),
    REPORT_CONFIG("report-config"),
    OUTPUT("output")
}

class DefinedOptions {
    private val optionParser = OptionParser()

    private val cmdShowHelp: OptionSpec<Void>
    private val cmdShowVersion: OptionSpec<Void>

    private val baselineDpmDb: OptionSpec<Path>
    private val currentDpmDb: OptionSpec<Path>

    private val reportConfig: OptionSpec<Path>

    private val output: OptionSpec<Path>
    private val forceOverwrite: OptionSpec<Void>

    private val verbosity: OptionSpec<OutputVerbosity>

    init {
        cmdShowHelp = optionParser
            .accepts(
                "help",
                "show this help message"
            ).forHelp()

        cmdShowVersion = optionParser
            .accepts(
                "version",
                "show version information"
            )

        baselineDpmDb = optionParser
            .accepts(
                OptName.BASELINE_DPM_DB.nameString,
                "DPM database to use as baseline in difference reporting"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        currentDpmDb = optionParser
            .accepts(
                OptName.CURRENT_DPM_DB.nameString,
                "DPM database to use as current in difference reporting"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter())

        reportConfig = optionParser
            .accepts(
                OptName.REPORT_CONFIG.nameString,
                "configuration file for controlling difference report details"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter())

        output = optionParser
            .accepts(
                OptName.OUTPUT.nameString,
                "output file where to store report"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter())

        forceOverwrite = optionParser
            .accepts(
                "force-overwrite",
                "silently overwrites the possibly existing target file"
            )

        verbosity = optionParser
            .accepts(
                "verbosity",
                "status output verbosity, modes: ${OutputVerbosity.NORMAL}, ${OutputVerbosity.DEBUG}"
            )
            .withOptionalArg()
            .withValuesConvertedBy(VerbosityConverter())
            .defaultsTo(OutputVerbosity.NORMAL)
    }

    fun detectOptionsFromArgs(args: Array<String>): DetectedOptions {
        return try {
            doDetectOptions(args)
        } catch (exception: OptionException) {
            val cause = exception.cause

            if (cause is ValueConversionException) {
                throwFail("Option ${exception.options().first()}: ${cause.message}")
            } else {
                throwFail("${exception.message}")
            }
        }
    }

    fun printHelp(outWriter: PrintWriter) {
        optionParser.formatHelpWith(FixedOrderHelpFormatter())
        optionParser.printHelpOn(outWriter)
    }

    private fun doDetectOptions(args: Array<String>): DetectedOptions {
        val optionSet = optionParser.parse(*args)

        if (!optionSet.hasOptions()) {
            throwFail("No options given (-h will show valid options)")
        }

        return DetectedOptions(
            cmdShowHelp = optionSet.has(cmdShowHelp),
            cmdShowVersion = optionSet.has(cmdShowVersion),
            baselineDpmDbPath = optionSet.valueOf(baselineDpmDb),
            currentDpmDbPath = optionSet.valueOf(currentDpmDb),
            reportConfigPath = optionSet.valueOf(reportConfig),
            outputFilePath = optionSet.valueOf(output),
            forceOverwrite = optionSet.has(this.forceOverwrite),
            verbosity = optionSet.valueOf(verbosity)
        )
    }

    private class VerbosityConverter : EnumConverter<OutputVerbosity>(OutputVerbosity::class.java)

    private class FixedOrderHelpFormatter :
        BuiltinHelpFormatter(120, 4) {

        override fun format(options: Map<String, OptionDescriptor>): String {
            addRows(LinkedHashSet(options.values))
            return formattedHelpOutput()
        }
    }
}
