package fi.vm.dpm.diff.cli.integration

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareVkData´ Required")
internal class RequiredSectionTest : DpmDiffCliCompareTestBase(
    section = "Required",
    commonSetupSql = ""
) {

    @Test
    fun `Should report added Required when unique requirement row is added`() {
        executeVkDataCompareForSectionAndExpectSuccess(
            baselineSql =
            """
            INSERT INTO 'Required' ('FrameworkCode', 'TaxonomyCode', 'DatapointID')
            VALUES
                ('FC_A', 'TC_A', 'DP_A'),
                ('FC_C', 'TC_C', 'DP_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Required' ('FrameworkCode', 'TaxonomyCode', 'DatapointID')
            VALUES
                ('FC_A', 'TC_A', 'DP_A'),
                ('FC_B', 'TC_B', 'DP_B'),
                ('FC_C', 'TC_C', 'DP_C')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Required")).containsExactly(
                "FrameworkCode: FC_B",
                "TaxonomyCode: TC_B",
                "DatapointId: DP_B",
                "Change: ADDED",
                "Notes: CURRENT ROW: #- Rowid: 2"
            )
        }
    }

    @Test
    fun `Should report duplicate key alerts from CurrentDb when duplicate requirement row is added`() {
        executeVkDataCompareForSectionAndExpectSuccess(
            baselineSql =
            """
            INSERT INTO 'Required' ('FrameworkCode', 'TaxonomyCode', 'DatapointID')
            VALUES
                ('FC_A', 'TC_A', 'DP_A')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Required' ('FrameworkCode', 'TaxonomyCode', 'DatapointID')
            VALUES
                ('FC_A', 'TC_A', 'DP_A'),
                ('FC_A', 'TC_A', 'DP_A')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 2
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Required")).containsExactly(
                "FrameworkCode: FC_A",
                "TaxonomyCode: TC_A",
                "DatapointId: DP_A",
                "Change: DUPLICATE_KEY_ALERT",
                "Notes: CURRENT ROW: #- Rowid: 1",
                "-----------",
                "FrameworkCode: FC_A",
                "TaxonomyCode: TC_A",
                "DatapointId: DP_A",
                "Change: DUPLICATE_KEY_ALERT",
                "Notes: CURRENT ROW: #- Rowid: 2"
            )
        }
    }

    @Test
    fun `Should report removed Required when unique requirement row is removed`() {
        executeVkDataCompareForSectionAndExpectSuccess(
            baselineSql =
            """
            INSERT INTO 'Required' ('FrameworkCode', 'TaxonomyCode', 'DatapointID')
            VALUES
                ('FC_A', 'TC_A', 'DP_A'),
                ('FC_B', 'TC_B', 'DP_B'),
                ('FC_C', 'TC_C', 'DP_C')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Required' ('FrameworkCode', 'TaxonomyCode', 'DatapointID')
            VALUES
                ('FC_A', 'TC_A', 'DP_A'),
                ('FC_C', 'TC_C', 'DP_C')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 1
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Required")).containsExactly(
                "FrameworkCode: FC_B",
                "TaxonomyCode: TC_B",
                "DatapointId: DP_B",
                "Change: DELETED",
                "Notes: BASELINE ROW: #- Rowid: 2"
            )
        }
    }

    @Test
    fun `Should report duplicate key alerts from baseline when duplicate requirement is removed`() {
        executeVkDataCompareForSectionAndExpectSuccess(
            baselineSql =
            """
            INSERT INTO 'Required' ('FrameworkCode', 'TaxonomyCode', 'DatapointID')
            VALUES
                ('FC_A', 'TC_A', 'DP_A'),
                ('FC_B', 'TC_B', 'DP_B'),
                ('FC_B', 'TC_B', 'DP_B')
            """.trimLineStartsAndConsequentBlankLines(),

            currentSql =
            """
            INSERT INTO 'Required' ('FrameworkCode', 'TaxonomyCode', 'DatapointID')
            VALUES
                ('FC_A', 'TC_A', 'DP_A'),
                ('FC_B', 'TC_B', 'DP_B')
            """.trimLineStartsAndConsequentBlankLines(),

            expectedChanges = 2
        ) { _, outputFileContent ->

            assertThat(outputFileContent.transposeSectionSheetAsList("01_Required")).containsExactly(
                "FrameworkCode: FC_B",
                "TaxonomyCode: TC_B",
                "DatapointId: DP_B",
                "Change: DUPLICATE_KEY_ALERT",
                "Notes: BASELINE ROW: #- Rowid: 2",
                "-----------",
                "FrameworkCode: FC_B",
                "TaxonomyCode: TC_B",
                "DatapointId: DP_B",
                "Change: DUPLICATE_KEY_ALERT",
                "Notes: BASELINE ROW: #- Rowid: 3"
            )
        }
    }
}
