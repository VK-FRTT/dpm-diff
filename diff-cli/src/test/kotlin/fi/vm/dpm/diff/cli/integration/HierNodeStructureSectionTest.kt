package fi.vm.dpm.diff.cli.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ HierNodeStructure")
internal class HierNodeStructureSectionTest : DpmDiffCliCompareTestBase(
    section = "HierNodeStructure",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new HierarchyNode`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mHierarchyNode' WHERE ConceptID = 350",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node_Structure")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: ADDED",
                "ParentMemberCode: ",
                "ParentMemberCode(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "Level: ",
                "Level(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED when Current is missing a HierarchyNode`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "DELETE from 'mHierarchyNode' WHERE ConceptID = 350",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node_Structure")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: DELETED",
                "ParentMemberCode: ",
                "ParentMemberCode(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "Level: ",
                "Level(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when ParentMember value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchyNode' SET ParentMemberID = 150 WHERE ConceptID = 351",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node_Structure")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M2",
                "HierarchyNodeLabelFi: EDA HierarchyNode 2 (label fi)",
                "Change: MODIFIED",
                "ParentMemberCode: EDA-M1",
                "ParentMemberCode(baseline): EDA-M2",
                "Order: ",
                "Order(baseline): ",
                "Level: ",
                "Level(baseline): ",
                "Notes: MODIFIED: #- Parent Member Code"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when Order value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchyNode' SET 'Order' = 7 WHERE ConceptID = 350",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node_Structure")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: MODIFIED",
                "ParentMemberCode: ",
                "ParentMemberCode(baseline): ",
                "Order: 7",
                "Order(baseline): 1",
                "Level: ",
                "Level(baseline): ",
                "Notes: MODIFIED: #- Order"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when Level value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchyNode' SET Level = 5 WHERE ConceptID = 350",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node_Structure")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: MODIFIED",
                "ParentMemberCode: ",
                "ParentMemberCode(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "Level: 5",
                "Level(baseline): 1",
                "Notes: MODIFIED: #- Level"
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when HierarchyID is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchyNode' SET HierarchyID = NULL WHERE ConceptID = 350",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node_Structure")).containsExactly(
                "HierarchyCode: ",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: ADDED",
                "ParentMemberCode: ",
                "ParentMemberCode(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "Level: ",
                "Level(baseline): ",
                "Notes: CURRENT ROW: #- Hierarchy Id: null #- Member Id: 150 #- Hierarchy Node Label: Node 1",
                "-----------",
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: DELETED",
                "ParentMemberCode: ",
                "ParentMemberCode(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "Level: ",
                "Level(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when MemberID is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchyNode' SET MemberID = NULL WHERE ConceptID = 350",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node_Structure")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: ",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: ADDED",
                "ParentMemberCode: ",
                "ParentMemberCode(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "Level: ",
                "Level(baseline): ",
                "Notes: CURRENT ROW: #- Hierarchy Id: 300 #- Member Id: null #- Hierarchy Node Label: Node 1",
                "-----------",
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: DELETED",
                "ParentMemberCode: ",
                "ParentMemberCode(baseline): ",
                "Order: ",
                "Order(baseline): ",
                "Level: ",
                "Level(baseline): ",
                "Notes: "
            )
        }
    }
}
