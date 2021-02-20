package fi.vm.dpm.diff.cli.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--help´")
internal class HelpTest : DpmDiffCliIntegrationTestBase() {

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

    @Test
    fun `Should output informative message when no known option is given in command line`() {
        val args = arrayOf("foo", "bar")

        executeCliAndExpectFail(args) { _, errText ->

            assertThat(errText).containsSubsequence(
                "No options given (-h will show valid options)"
            )
        }
    }

    @Test
    fun `Should output message when given option is not known`() {
        val args = arrayOf("--unknownOption")

        executeCliAndExpectFail(args) { _, errText ->

            assertThat(errText).containsSubsequence(
                "unknownOption is not a recognized option"
            )
        }
    }

    @Test
    fun `Should output message when no command is recognized from command line`() {
        val args = arrayOf("--output", "test.xlsx")

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Single command must be given"
            )
        }
    }
}
