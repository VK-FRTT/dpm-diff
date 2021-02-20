package fi.vm.dpm.diff.cli.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--version´")
internal class VersionTest : DpmDiffCliIntegrationTestBase() {

    @Test
    fun `Should list version information`() {
        val args = arrayOf("--version")

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM Diff",
                "Build time:   dev-build-time",
                "Revision:     dev-revision",
                "Origin URL:   dev-origin"
            )
        }
    }
}
