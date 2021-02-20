package fi.vm.dpm.diff.cli.integration

import ext.kotlin.trimLineStartsAndConsequentBlankLines
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
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('B', 'DPS_B', 'DPSF_B', 'OC_B'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: B",
                "Change: ADDED",
                "Dps: DPS_B",
                "Dps(baseline): ",
                "DpsFull: DPSF_B",
                "DpsFull(baseline): ",
                "OpenContext: OC_B",
                "OpenContext(baseline): ",
                "Notes: CURRENT ROW: #- Rowid: 2"
            )
        }
    }

    @Test
    fun `Should report added DataPointId when duplicate ID is added`() {
        executeVkDataCompareForSectionAndExpectSuccess(

            baselineSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('B', 'DPS_B', 'DPSF_B', 'OC_B')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('A', 'DPS_A2', 'DPSF_A2', 'OC_A2'),
                ('B', 'DPS_B', 'DPSF_B', 'OC_B')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: A",
                "Change: ADDED",
                "Dps: DPS_A2",
                "Dps(baseline): ",
                "DpsFull: DPSF_A2",
                "DpsFull(baseline): ",
                "OpenContext: OC_A2",
                "OpenContext(baseline): ",
                "Notes: CURRENT ROW: #- Rowid: 2"
            )
        }
    }

    @Test
    fun `Should report removed DataPointId when unique ID is removed`() {
        executeVkDataCompareForSectionAndExpectSuccess(

            baselineSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('B', 'DPS_B', 'DPSF_B', 'OC_B'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: B",
                "Change: DELETED",
                "Dps: ",
                "Dps(baseline): DPS_B",
                "DpsFull: ",
                "DpsFull(baseline): DPSF_B",
                "OpenContext: ",
                "OpenContext(baseline): OC_B",
                "Notes: BASELINE ROW: #- Rowid: 2"
            )
        }
    }

    @Test
    fun `Should report removed DataPointId when duplicate ID is removed`() {
        executeVkDataCompareForSectionAndExpectSuccess(

            baselineSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('A', 'DPS_A2', 'DPSF_A2', 'OC_A2'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A2', 'DPSF_A2', 'OC_A2'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Datapoint_ID")).containsExactly(
                "DatapointId: A",
                "Change: DELETED",
                "Dps: ",
                "Dps(baseline): DPS_A",
                "DpsFull: ",
                "DpsFull(baseline): DPSF_A",
                "OpenContext: ",
                "OpenContext(baseline): OC_A",
                "Notes: BASELINE ROW: #- Rowid: 1"
            )
        }
    }

    @Test
    fun `Should execute 1 partition when DBs have 500_000 rows`() {
        val initSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ${(1..500_000).map { "('$it', 'DPS_$it', 'DPSF_$it', 'OC_$it')" }.joinToString(separator = ",\n")}
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
                "Dps: ",
                "Dps(baseline): ",
                "DpsFull: ",
                "DpsFull(baseline): ",
                "OpenContext: ",
                "OpenContext(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should execute 2 partitions when DBs have more than 500_000 rows`() {
        val baselineInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ${(1..500_000).map { "('$it', 'DPS_$it', 'DPSF_$it', 'OC_$it')" }.joinToString(separator = ",\n")}
            """.trimLineStartsAndConsequentBlankLines()

        val currentInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ${(1..500_002).map { "('$it', 'DPS_$it', 'DPSF_$it', 'OC_$it')" }.joinToString(separator = ",\n")}
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
                "Dps: DPS_500001",
                "Dps(baseline): ",
                "DpsFull: DPSF_500001",
                "DpsFull(baseline): ",
                "OpenContext: OC_500001",
                "OpenContext(baseline): ",
                "Notes: CURRENT ROW: #- Rowid: 500001",
                "-----------",
                "DatapointId: 500002",
                "Change: ADDED",
                "Dps: DPS_500002",
                "Dps(baseline): ",
                "DpsFull: DPSF_500002",
                "DpsFull(baseline): ",
                "OpenContext: OC_500002",
                "OpenContext(baseline): ",
                "Notes: CURRENT ROW: #- Rowid: 500002"
            )
        }
    }
}
