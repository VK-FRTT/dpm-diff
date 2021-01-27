package fi.vm.dpm.diff.cli

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareVkData´ DatapointIDs")
internal class CommandVkData_DataPointIdSection_Test : DpmDiffCli_TestBase() {

    @Test
    fun `Should report added DataPointId when unique ID is added`() {
        executeVkDataCompareWithSpreadsheetOutputAndExpectSuccess(
            section = "DatapointID",

            baselineInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('B', 'DPS_B', 'DPSF_B', 'OC_B'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines()
        ) { cliOutput, outputFileContent ->

            assertThat(cliOutput).containsSubsequence(
                "Section: Datapoint IDs",
                "Total changes: 1"
            )

            assertThat(outputFileContent.sectionSheetAsStringList("01_Datapoint_ID")).containsExactly(
                "#DATAPOINT ID, #CHANGE, #DPS, #DPS (BASELINE), #DPS FULL, #DPS FULL (BASELINE), #OPEN CONTEXT, #OPEN CONTEXT (BASELINE), #NOTES",
                "B, ADDED, DPS_B, , DPSF_B, , OC_B, , CURRENT ROW: - Rowid: 2"
            )
        }
    }

    @Test
    fun `Should report added DataPointId when duplicate ID is added`() {
        executeVkDataCompareWithSpreadsheetOutputAndExpectSuccess(
            section = "DatapointID",

            baselineInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('B', 'DPS_B', 'DPSF_B', 'OC_B')
            """.trimLineStartsAndConsequentBlankLines(),

            currentInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('A', 'DPS_A2', 'DPSF_A2', 'OC_A2'),
                ('B', 'DPS_B', 'DPSF_B', 'OC_B')
            """.trimLineStartsAndConsequentBlankLines()
        ) { cliOutput, outputFileContent ->

            assertThat(cliOutput).containsSubsequence(
                "Section: Datapoint IDs",
                "Total changes: 1"
            )

            assertThat(outputFileContent.sectionSheetAsStringList("01_Datapoint_ID")).containsExactly(
                "#DATAPOINT ID, #CHANGE, #DPS, #DPS (BASELINE), #DPS FULL, #DPS FULL (BASELINE), #OPEN CONTEXT, #OPEN CONTEXT (BASELINE), #NOTES",
                "A, ADDED, DPS_A2, , DPSF_A2, , OC_A2, , CURRENT ROW: - Rowid: 2"
            )
        }
    }

    @Test
    fun `Should report removed DataPointId when unique ID is removed`() {
        executeVkDataCompareWithSpreadsheetOutputAndExpectSuccess(
            section = "DatapointID",

            baselineInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('B', 'DPS_B', 'DPSF_B', 'OC_B'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines()
        ) { cliOutput, outputFileContent ->

            assertThat(cliOutput).containsSubsequence(
                "Section: Datapoint IDs",
                "Total changes: 1"
            )

            assertThat(outputFileContent.sectionSheetAsStringList("01_Datapoint_ID")).containsExactly(
                "#DATAPOINT ID, #CHANGE, #DPS, #DPS (BASELINE), #DPS FULL, #DPS FULL (BASELINE), #OPEN CONTEXT, #OPEN CONTEXT (BASELINE), #NOTES",
                "B, DELETED, , DPS_B, , DPSF_B, , OC_B, BASELINE ROW: - Rowid: 2"
            )
        }
    }

    @Test
    fun `Should report removed DataPointId when duplicate ID is removed`() {
        executeVkDataCompareWithSpreadsheetOutputAndExpectSuccess(
            section = "DatapointID",

            baselineInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A', 'DPSF_A', 'OC_A'),
                ('A', 'DPS_A2', 'DPSF_A2', 'OC_A2'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentInitSql =
            """
            INSERT INTO 'Kenttatunnukset' ('DatapointID', 'DPS', 'DPS_Full', 'OpenContext')
            VALUES
                ('A', 'DPS_A2', 'DPSF_A2', 'OC_A2'),
                ('C', 'DPS_C', 'DPSF_C', 'OC_C')
            """.trimLineStartsAndConsequentBlankLines()
        ) { cliOutput, outputFileContent ->

            assertThat(cliOutput).containsSubsequence(
                "Section: Datapoint IDs",
                "Total changes: 1"
            )

            assertThat(outputFileContent.sectionSheetAsStringList("01_Datapoint_ID")).containsExactly(
                "#DATAPOINT ID, #CHANGE, #DPS, #DPS (BASELINE), #DPS FULL, #DPS FULL (BASELINE), #OPEN CONTEXT, #OPEN CONTEXT (BASELINE), #NOTES",
                "A, DELETED, , DPS_A, , DPSF_A, , OC_A, BASELINE ROW: - Rowid: 1"
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

        executeVkDataCompareWithSpreadsheetOutputAndExpectSuccess(
            section = "DatapointID",
            baselineInitSql = initSql,
            currentInitSql = initSql
        ) { cliOutput, outputFileContent ->

            assertThat(cliOutput).containsSubsequence(
                "Section: Datapoint IDs",
                "Loading baseline records |     1 |",
                "Loading current records |     1 |",
                "Baseline records: 500000",
                "Current records: 500000",
                "Total changes: 0"
            )

            assertThat(outputFileContent.sectionSheetAsStringList("01_Datapoint_ID")).containsExactly(
                "#DATAPOINT ID, #CHANGE, #DPS, #DPS (BASELINE), #DPS FULL, #DPS FULL (BASELINE), #OPEN CONTEXT, #OPEN CONTEXT (BASELINE), #NOTES"
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

        executeVkDataCompareWithSpreadsheetOutputAndExpectSuccess(
            section = "DatapointID",
            baselineInitSql = baselineInitSql,
            currentInitSql = currentInitSql
        ) { cliOutput, outputFileContent ->

            assertThat(cliOutput).containsSubsequence(
                "Section: Datapoint IDs",
                "Loading baseline records |     2 |",
                "Loading current records |     2 |",
                "Baseline records: 500000",
                "Current records: 500002",
                "Total changes: 2"
            )

            assertThat(outputFileContent.sectionSheetAsStringList("01_Datapoint_ID")).containsExactly(
                "#DATAPOINT ID, #CHANGE, #DPS, #DPS (BASELINE), #DPS FULL, #DPS FULL (BASELINE), #OPEN CONTEXT, #OPEN CONTEXT (BASELINE), #NOTES",
                "500001, ADDED, DPS_500001, , DPSF_500001, , OC_500001, , CURRENT ROW: - Rowid: 500001",
                "500002, ADDED, DPS_500002, , DPSF_500002, , OC_500002, , CURRENT ROW: - Rowid: 500002"
            )
        }
    }
}
