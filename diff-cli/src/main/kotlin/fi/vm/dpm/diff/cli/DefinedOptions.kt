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
    BASELINE_DB("baselineDb"),
    CURRENT_DB("currentDb"),
    OUTPUT("output"),
    IDENTIFICATION_LABEL_LANGUAGES("identificationLabelLanguages"),
    TRANSLATION_LANGUAGES("translationLanguages")
}

class DefinedOptions {
    private val optionParser = OptionParser()

    private val cmdShowHelp: OptionSpec<Void>
    private val cmdShowVersion: OptionSpec<Void>
    private val cmdCompareDpm: OptionSpec<Void>
    private val cmdCompareVkData: OptionSpec<Void>

    private val baselineDb: OptionSpec<Path>
    private val currentDb: OptionSpec<Path>
    private val output: OptionSpec<Path>
    private val forceOverwrite: OptionSpec<Void>
    private val verbosity: OptionSpec<OutputVerbosity>

    private val identificationLabelLanguages: OptionSpec<String>
    private val translationLanguages: OptionSpec<String>

    init {

        // Commands
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

        cmdCompareDpm = optionParser
            .accepts(
                "compareDpm",
                "compare Data Point Models"
            )

        cmdCompareVkData = optionParser
            .accepts(
                "compareVkData",
                "compare VK Data"
            )

        // Common options
        baselineDb = optionParser
            .accepts(
                OptName.BASELINE_DB.nameString,
                "Database to use as baseline in difference reporting"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        currentDb = optionParser
            .accepts(
                OptName.CURRENT_DB.nameString,
                "Database to use as current in difference reporting"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        output = optionParser
            .accepts(
                OptName.OUTPUT.nameString,
                "output file where to store report"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        forceOverwrite = optionParser
            .accepts(
                "force-overwrite",
                "silently overwrites the possibly existing target file"
            )

        verbosity = optionParser
            .accepts(
                "verbosity",
                "execution output verbosity, modes: ${OutputVerbosity.NORMAL}, ${OutputVerbosity.DEBUG}"
            )
            .withOptionalArg()
            .withValuesConvertedBy(VerbosityConverter())
            .defaultsTo(OutputVerbosity.NORMAL)

        // CompareDpm specific options
        identificationLabelLanguages = optionParser
            .accepts(
                OptName.IDENTIFICATION_LABEL_LANGUAGES.nameString,
                "list of identification label languages to include in generated report"
            )
            .withOptionalArg()

        translationLanguages = optionParser
            .accepts(
                OptName.TRANSLATION_LANGUAGES.nameString,
                "list of languages for which translation changes are reported"
            )
            .withOptionalArg()
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
            cmdCompareDpm = optionSet.has(cmdCompareDpm),
            cmdCompareVkData = optionSet.has(cmdCompareVkData),

            baselineDpmDbPath = optionSet.valueOf(baselineDb),
            currentDpmDbPath = optionSet.valueOf(currentDb),
            outputFilePath = optionSet.valueOf(output),
            forceOverwrite = optionSet.has(forceOverwrite),
            verbosity = optionSet.valueOf(verbosity),

            identificationLabelLanguages = if (optionSet.has(identificationLabelLanguages)) {
                optionSet.valueOf(identificationLabelLanguages)
            } else {
                null
            },

            translationLanguages = if (optionSet.has(translationLanguages)) {
                optionSet.valueOf(translationLanguages)
            } else {
                null
            }
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
