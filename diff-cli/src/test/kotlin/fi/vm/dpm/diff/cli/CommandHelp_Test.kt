package fi.vm.dpm.diff.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--help´")
internal class CommandHelp_Test : DpmDiffCli_TestBase() {

    @Test
    fun `Should list available command line options`() {
        val args = arrayOf("--help")

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "--help",
                "--version",
                "--compareDpm",
                "--compareVkData",
                "--baselineDb",
                "--currentDb",
                "--output",
                "--forceOverwrite",
                "--verbosity",
                "--reportSections",
                "--identificationLabelLanguages",
                "--translationLanguages"
            )
        }
    }
}
