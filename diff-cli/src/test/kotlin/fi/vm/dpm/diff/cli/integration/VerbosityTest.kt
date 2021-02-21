package fi.vm.dpm.diff.cli.integration

import fi.vm.dpm.diff.cli.integration.comparedpm.compareDpmSetupSql
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Option ´--verbosity´")
internal class VerbosityTest : DpmDiffCliCompareTestBase(
    section = "Domain",
    commonSetupSql = compareDpmSetupSql()
) {
    private val normalMessages = listOf(
        "DPM Diff",
        "Section: Domain",
        "Progress:",
        "Baseline records:",
        "Current records:",
        "Total changes:",
        "Writing report to:",
        "Report done!"
    )

    private val verboseMessages = listOf(
        "Section generation metrics:"
    )

    private val debugMessages = listOf(
        "Query Domains SourceRecordsPartition 1/1 BASELINE:",
        "QueryColumnMapping validation: OK",
        "Query Domains SourceRecordsPartition 1/1 CURRENT:",
        "QueryColumnMapping validation: OK",
        "SourceRecord count validation (BASELINE): OK",
        "SourceRecord count validation (CURRENT): OK"
    )

    @Test
    fun `Should output normal execution information when verbosity is NORMAL`() {
        executeDpmCompareForSectionAndExpectSuccess(
            verbosity = "NORMAL",
            baselineSql = "DELETE from 'mDomain' WHERE DomainID = 1",
            expectedChanges = 1
        ) { outText, _ ->
            assertThat(outText).containsSubsequence("--verbosity", "NORMAL")
            assertThat(outText).containsSubsequence(normalMessages)
        }
    }

    @Test
    fun `Should output normal execution information when verbosity is not given via command line options`() {
        executeDpmCompareForSectionAndExpectSuccess(
            verbosity = null,
            baselineSql = "DELETE from 'mDomain' WHERE DomainID = 1",
            expectedChanges = 1
        ) { outText, _ ->
            assertThat(outText).doesNotContain("--verbosity", "NORMAL")
            assertThat(outText).containsSubsequence(normalMessages)
        }
    }

    @Test
    fun `Should output normal & verbose execution information when verbosity is VERBOSE`() {
        executeDpmCompareForSectionAndExpectSuccess(
            verbosity = "VERBOSE",
            baselineSql = "DELETE from 'mDomain' WHERE DomainID = 1",
            expectedChanges = 1
        ) { outText, _ ->
            assertThat(outText).containsSubsequence("--verbosity", "VERBOSE")
            assertThat(outText).containsSubsequence(normalMessages)
            assertThat(outText).containsSubsequence(verboseMessages)
        }
    }

    @Test
    fun `Should output normal, verbose & debug execution information when verbosity is DEBUG`() {
        executeDpmCompareForSectionAndExpectSuccess(
            verbosity = "DEBUG",
            baselineSql = "DELETE from 'mDomain' WHERE DomainID = 1",
            expectedChanges = 1
        ) { outText, _ ->
            assertThat(outText).containsSubsequence("--verbosity", "DEBUG")
            assertThat(outText).containsSubsequence(normalMessages)
            assertThat(outText).containsSubsequence(verboseMessages)
            assertThat(outText).containsSubsequence(debugMessages)
        }
    }

    @Test
    fun `Should output error message when verbosity option is empty`() {
        setupDpmCompareFixtures()

        val args = buildDpmCompareArgs(
            verbosity = ""
        )

        executeCliAndExpectFail(
            args = args
        ) { _, errText ->
            assertThat(errText).containsSubsequence(
                "Option verbosity: Value [] is not one of [[NORMAL,VERBOSE,DEBUG]]"
            )
        }
    }
}
