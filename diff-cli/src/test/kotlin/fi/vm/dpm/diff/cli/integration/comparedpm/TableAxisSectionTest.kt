package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ TableAxis")
internal class TableAxisSectionTest : DpmDiffCliCompareTestBase(
    section = "TableAxis",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new TableAxis`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mTableAxis' WHERE AxisID = 1000",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table_Axis")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "AxisLabelFi: AXA axis (label fi)",
                "Change: ADDED",
                "Order: ",
                "Order(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED when Current is missing a TableAxis`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "DELETE from 'mTableAxis' WHERE AxisID = 1000",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table_Axis")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "AxisLabelFi: AXA axis (label fi)",
                "Change: DELETED",
                "Order: ",
                "Order(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when AxisOrientation is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mAxis' SET AxisOrientation = NULL WHERE AxisID = 1000",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table_Axis")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: ",
                "AxisLabelFi: AXA axis (label fi)",
                "Change: ADDED",
                "Order: ",
                "Order(baseline): ",
                "Notes: CURRENT ROW: #- Axis Id: 1000 #- Axis Label: AXA axis",
                "-----------",
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "AxisLabelFi: AXA axis (label fi)",
                "Change: DELETED",
                "Order: ",
                "Order(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when Order value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mTableAxis' SET 'Order' = 2 WHERE AxisID = 1000 AND TableID = 800",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table_Axis")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "AxisLabelFi: AXA axis (label fi)",
                "Change: MODIFIED",
                "Order: 2",
                "Order(baseline): 1",
                "Notes: MODIFIED: #- Order"
            )
        }
    }
}
