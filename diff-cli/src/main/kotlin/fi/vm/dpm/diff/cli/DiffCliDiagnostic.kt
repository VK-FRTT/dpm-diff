package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.model.throwHalt
import java.io.PrintWriter

class DiffCliDiagnostic(
    private val printWriter: PrintWriter,
    private val verbosity: OutputVerbosity
) : Diagnostic {

    override fun info(message: String) {
        printWriter.println(message)
    }

    override fun infoStepProgress() {
        printWriter.print(".")
        printWriter.flush()
    }

    override fun verbose(message: String) {
        if (verbosity >= OutputVerbosity.VERBOSE) {
            printWriter.println(message)
        }
    }

    override fun debug(message: String) {
        if (verbosity >= OutputVerbosity.DEBUG) {
            printWriter.println(message)
        }
    }

    override fun fatal(message: String): Nothing {
        printWriter.println("\nError:")
        printWriter.println(message)

        throwHalt()
    }
}
