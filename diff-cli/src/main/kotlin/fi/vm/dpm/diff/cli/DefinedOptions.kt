package fi.vm.dpm.diff.cli

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
import joptsimple.util.PathProperties

enum class OptName(val nameString: String) {
    BASELINE_DPM_DB("baseline-dpm-db"),
    CHANGED_DPM_DB("changed-dpm-db"),
    REPORT_CONFIG("report-config")
}

class DefinedOptions {
    private val optionParser = OptionParser()

    private val cmdShowHelp: OptionSpec<Void>
    private val cmdShowVersion: OptionSpec<Void>

    private val baselineDpmDb: OptionSpec<Path>
    private val changedDpmDb: OptionSpec<Path>

    private val reportConfig: OptionSpec<Path>

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
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        changedDpmDb = optionParser
            .accepts(
                OptName.CHANGED_DPM_DB.nameString,
                "second (the updated) DPM database for difference reporting"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        reportConfig = optionParser
            .accepts(
                OptName.REPORT_CONFIG.nameString,
                "configuration file for controlling difference report details"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

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
            baselineDpmDb = optionSet.valueOf(baselineDpmDb),
            changedDpmDb = optionSet.valueOf(changedDpmDb),
            reportConfig = optionSet.valueOf(reportConfig),
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
