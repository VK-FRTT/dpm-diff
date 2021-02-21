package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ HierNode")
internal class HierNodeSectionTest : DpmDiffCliCompareTestBase(
    section = "HierNode",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new HierarchyNode`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mHierarchyNode' WHERE ConceptID = 350",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: ADDED",
                "IsAbstract: ",
                "IsAbstract(baseline): ",
                "ComparisonOperator: ",
                "ComparisonOperator(baseline): ",
                "UnaryOperator: ",
                "UnaryOperator(baseline): ",
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
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: DELETED",
                "IsAbstract: ",
                "IsAbstract(baseline): ",
                "ComparisonOperator: ",
                "ComparisonOperator(baseline): ",
                "UnaryOperator: ",
                "UnaryOperator(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when IsAbstract value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchyNode' SET IsAbstract = 1 WHERE ConceptID = 350",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: MODIFIED",
                "IsAbstract: 1",
                "IsAbstract(baseline): 0",
                "ComparisonOperator: ",
                "ComparisonOperator(baseline): ",
                "UnaryOperator: ",
                "UnaryOperator(baseline): ",
                "Notes: MODIFIED: #- Is Abstract"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when ComparisonOperator value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchyNode' SET ComparisonOperator = '<>' WHERE ConceptID = 350",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: MODIFIED",
                "IsAbstract: ",
                "IsAbstract(baseline): ",
                "ComparisonOperator: <>",
                "ComparisonOperator(baseline): >",
                "UnaryOperator: ",
                "UnaryOperator(baseline): ",
                "Notes: MODIFIED: #- Comparison Operator"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when UnaryOperator value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchyNode' SET UnaryOperator = '-' WHERE ConceptID = 350",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: MODIFIED",
                "IsAbstract: ",
                "IsAbstract(baseline): ",
                "ComparisonOperator: ",
                "ComparisonOperator(baseline): ",
                "UnaryOperator: -",
                "UnaryOperator(baseline): +",
                "Notes: MODIFIED: #- Unary Operator"
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when HierarchyID is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchyNode' SET HierarchyID = NULL WHERE ConceptID = 350",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node")).containsExactly(
                "HierarchyCode: ",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: ADDED",
                "IsAbstract: ",
                "IsAbstract(baseline): ",
                "ComparisonOperator: ",
                "ComparisonOperator(baseline): ",
                "UnaryOperator: ",
                "UnaryOperator(baseline): ",
                "Notes: CURRENT ROW: #- Hierarchy Id: null #- Member Id: 150 #- Hierarchy Node Label: Node 1",
                "-----------",
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: DELETED",
                "IsAbstract: ",
                "IsAbstract(baseline): ",
                "ComparisonOperator: ",
                "ComparisonOperator(baseline): ",
                "UnaryOperator: ",
                "UnaryOperator(baseline): ",
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
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hier_Node")).containsExactly(
                "HierarchyCode: EDA-H1",
                "MemberCode: ",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: ADDED",
                "IsAbstract: ",
                "IsAbstract(baseline): ",
                "ComparisonOperator: ",
                "ComparisonOperator(baseline): ",
                "UnaryOperator: ",
                "UnaryOperator(baseline): ",
                "Notes: CURRENT ROW: #- Hierarchy Id: 300 #- Member Id: null #- Hierarchy Node Label: Node 1",
                "-----------",
                "HierarchyCode: EDA-H1",
                "MemberCode: EDA-M1",
                "HierarchyNodeLabelFi: EDA HierarchyNode 1 (label fi)",
                "Change: DELETED",
                "IsAbstract: ",
                "IsAbstract(baseline): ",
                "ComparisonOperator: ",
                "ComparisonOperator(baseline): ",
                "UnaryOperator: ",
                "UnaryOperator(baseline): ",
                "Notes: "
            )
        }
    }
}
