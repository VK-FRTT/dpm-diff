package fi.vm.dpm.diff.cli.integration.comparevkdata

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareVkData´ DatapointID")
internal class DataPointIdSectionTest : DpmDiffCliCompareTestBase(
    section = "DatapointID",
    commonSetupSql = ""
) {

    @Test
    fun `Should report added DataPointId when unique ID is added`() {
        executeVkDataCompareForSectionAndExpectSuccess(

            baselineSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ('A', 'DPSF_A'),
                ('C', 'DPSF_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ('A', 'DPSF_A'),
                ('B', 'DPSF_B'),
                ('C', 'DPSF_C')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: B",
                "Change: ADDED",
                "DpsFull: DPSF_B",
                "DpsFull(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report added DataPointId when duplicate ID is added`() {
        executeVkDataCompareForSectionAndExpectSuccess(

            baselineSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ('A', 'DPSF_A'),
                ('B', 'DPSF_B')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ('A', 'DPSF_A'),
                ('A', 'DPSF_A2'),
                ('B', 'DPSF_B')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: A",
                "Change: ADDED",
                "DpsFull: DPSF_A2",
                "DpsFull(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report removed DataPointId when unique ID is removed`() {
        executeVkDataCompareForSectionAndExpectSuccess(

            baselineSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ('A', 'DPSF_A'),
                ('B', 'DPSF_B'),
                ('C', 'DPSF_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ('A', 'DPSF_A'),
                ('C', 'DPSF_C')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: B",
                "Change: DELETED",
                "DpsFull: ",
                "DpsFull(baseline): DPSF_B",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report removed DataPointId when duplicate ID is removed`() {
        executeVkDataCompareForSectionAndExpectSuccess(

            baselineSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ('A', 'DPSF_A'),
                ('A', 'DPSF_A2'),
                ('C', 'DPSF_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ('A', 'DPSF_A2'),
                ('C', 'DPSF_C')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: A",
                "Change: DELETED",
                "DpsFull: ",
                "DpsFull(baseline): DPSF_A",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should execute 1 partition when DBs have 500_000 rows`() {
        val initSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ${(1..500_000).map { "('$it', 'DPSF_$it')" }.joinToString(separator = ",\n")}
            """.trimLineStartsAndConsequentBlankLines()

        executeVkDataCompareForSectionAndExpectSuccess(
            baselineSql = initSql,
            currentSql = initSql,
            expectedChanges = 0
        ) { cliOutput, outputFileContent ->

            assertThat(cliOutput).containsSubsequence(
                "Section: DatapointID",
                "Loading baseline records |     1 |",
                "Loading current records |     1 |",
                "Baseline records: 500000",
                "Current records: 500000",
                "Total changes: 0"
            )

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: ",
                "Change: ",
                "DpsFull: ",
                "DpsFull(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should execute 2 partitions when DBs have more than 500_000 rows`() {
        val baselineInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ${(1..500_000).map { "('$it', 'DPSF_$it')" }.joinToString(separator = ",\n")}
            """.trimLineStartsAndConsequentBlankLines()

        val currentInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS_Full')
            VALUES
                ${(1..500_002).map { "('$it', 'DPSF_$it')" }.joinToString(separator = ",\n")}
            """.trimLineStartsAndConsequentBlankLines()

        executeVkDataCompareForSectionAndExpectSuccess(
            baselineSql = baselineInitSql,
            currentSql = currentInitSql,
            expectedChanges = 2
        ) { cliOutput, outputFileContent ->

            assertThat(cliOutput).containsSubsequence(
                "Section: DatapointID",
                "Loading baseline records |     2 |",
                "Loading current records |     2 |",
                "Baseline records: 500000",
                "Current records: 500002",
                "Total changes: 2"
            )

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: 500001",
                "Change: ADDED",
                "DpsFull: DPSF_500001",
                "DpsFull(baseline): ",
                "Notes: ",
                "-----------",
                "DatapointId: 500002",
                "Change: ADDED",
                "DpsFull: DPSF_500002",
                "DpsFull(baseline): ",
                "Notes: "
            )
        }
    }
}
