package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.FailException
import fi.vm.dpm.diff.model.HaltException
import java.io.PrintWriter

object DiffCliExceptionHarness {

    fun withExceptionHarness(
        errWriter: PrintWriter,
        action: () -> Unit
    ): Int {
        return try {
            action()
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
}
