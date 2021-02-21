package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ AxisOrdinate")
internal class AxisOrdinateSectionTest : DpmDiffCliCompareTestBase(
    section = "AxisOrdinate",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new AxisOrdinate`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mAxisOrdinate' WHERE OrdinateID = 1101",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: ADDED",
                "Level: ",
                "Level(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED when Current is missing a AxisOrdinate`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "DELETE from 'mAxisOrdinate' WHERE OrdinateID = 1101",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: DELETED",
                "Level: ",
                "Level(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when OrdinateCode is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mAxisOrdinate' SET OrdinateCode = NULL WHERE OrdinateID = 1101",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: ",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: ADDED",
                "Level: ",
                "Level(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: CURRENT ROW: #- Ordinate Id: 1101 #- Ordinate Label: AOB axis ordinate",
                "-----------",
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: DELETED",
                "Level: ",
                "Level(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when Level value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mAxisOrdinate' SET Level = 99 WHERE OrdinateID = 1101",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: MODIFIED",
                "Level: 99",
                "Level(baseline): 10",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: MODIFIED: #- Level"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when Order value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mAxisOrdinate' SET 'Order' = 99 WHERE OrdinateID = 1101",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: MODIFIED",
                "Level: ",
                "Level(baseline): ",
                "Order: 99",
                "Order(baseline): 20",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: MODIFIED: #- Order"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when ParentOrdinate reference is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mAxisOrdinate' SET ParentOrdinateID = NULL WHERE OrdinateID = 1101",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: MODIFIED",
                "Level: ",
                "Level(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): AOA",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: MODIFIED: #- Parent Ordinate Code"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when IsDisplayBeforeChildren value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mAxisOrdinate' SET IsDisplayBeforeChildren = 1 WHERE OrdinateID = 1101",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: MODIFIED",
                "Level: ",
                "Level(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: 1",
                "IsDisplayBeforeChildren(baseline): 0",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: MODIFIED: #- Is Display Before Children"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when IsAbstractHeader value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mAxisOrdinate' SET IsAbstractHeader = 1 WHERE OrdinateID = 1101",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: MODIFIED",
                "Level: ",
                "Level(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: 1",
                "IsAbstractHeader(baseline): 0",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: MODIFIED: #- Is Abstract Header"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when IsRowKey value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mAxisOrdinate' SET IsRowKey = 1 WHERE OrdinateID = 1101",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: MODIFIED",
                "Level: ",
                "Level(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: 1",
                "IsRowKey(baseline): 0",
                "TypeOfKey: ",
                "TypeOfKey(baseline): ",
                "Notes: MODIFIED: #- Is Row Key"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when TypeOfKey value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mAxisOrdinate' SET TypeOfKey = 'type from test' WHERE OrdinateID = 1101",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ordinate")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "Change: MODIFIED",
                "Level: ",
                "Level(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "ParentOrdinateCode: ",
                "ParentOrdinateCode(baseline): ",
                "IsDisplayBeforeChildren: ",
                "IsDisplayBeforeChildren(baseline): ",
                "IsAbstractHeader: ",
                "IsAbstractHeader(baseline): ",
                "IsRowKey: ",
                "IsRowKey(baseline): ",
                "TypeOfKey: type from test",
                "TypeOfKey(baseline): type-of-key-B",
                "Notes: MODIFIED: #- Type Of Key"
            )
        }
    }
}
