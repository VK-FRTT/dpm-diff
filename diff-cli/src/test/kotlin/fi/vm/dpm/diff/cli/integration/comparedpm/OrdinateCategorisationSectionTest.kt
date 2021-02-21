package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ OrdCat")
internal class OrdinateCategorisationSectionTest : DpmDiffCliCompareTestBase(
    section = "OrdCat",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new OrdinateCategorisation`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mOrdinateCategorisation' WHERE OrdinateID = 1100",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Ord_Cat")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOA",
                "DimensionCode: EDA-DIM",
                "MemberCode: EDA-M1",
                "Change: ADDED",
                "Source: ",
                "Source(baseline): ",
                "Dps: ",
                "Dps(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED when Current is missing a OrdinateCategorisation`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "DELETE from 'mOrdinateCategorisation' WHERE OrdinateID = 1100",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Ord_Cat")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOA",
                "DimensionCode: EDA-DIM",
                "MemberCode: EDA-M1",
                "Change: DELETED",
                "Source: ",
                "Source(baseline): ",
                "Dps: ",
                "Dps(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when DimensionID is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mOrdinateCategorisation' SET DimensionID = NULL WHERE OrdinateID = 1100",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Ord_Cat")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOA",
                "DimensionCode: ",
                "MemberCode: EDA-M1",
                "Change: ADDED",
                "Source: ",
                "Source(baseline): ",
                "Dps: ",
                "Dps(baseline): ",
                "Notes: CURRENT ROW: #- Ordinate Id: 1100 #- Dimension Id: null #- Member Id: 150",
                "-----------",
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOA",
                "DimensionCode: EDA-DIM",
                "MemberCode: EDA-M1",
                "Change: DELETED",
                "Source: ",
                "Source(baseline): ",
                "Dps: ",
                "Dps(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when Source value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mOrdinateCategorisation' SET Source = 'from test' WHERE OrdinateID = 1100",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Ord_Cat")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOA",
                "DimensionCode: EDA-DIM",
                "MemberCode: EDA-M1",
                "Change: MODIFIED",
                "Source: from test",
                "Source(baseline): source-A",
                "Dps: ",
                "Dps(baseline): ",
                "Notes: MODIFIED: #- Source"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when Dps value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mOrdinateCategorisation' SET Dps = 'from test' WHERE OrdinateID = 1100",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Ord_Cat")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOA",
                "DimensionCode: EDA-DIM",
                "MemberCode: EDA-M1",
                "Change: MODIFIED",
                "Source: ",
                "Source(baseline): ",
                "Dps: from test",
                "Dps(baseline): dps-A",
                "Notes: MODIFIED: #- Dps"
            )
        }
    }
}
