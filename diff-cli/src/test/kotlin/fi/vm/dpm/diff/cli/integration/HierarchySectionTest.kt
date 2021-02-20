package fi.vm.dpm.diff.cli.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ Hierarchy")
internal class HierarchySectionTest : DpmDiffCliCompareTestBase(
    section = "Hierarchy",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new Hierarchy`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mHierarchy' WHERE HierarchyID = 300",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hierarchy")).containsExactly(
                "DomainCode: EDA",
                "HierarchyCode: EDA-H1",
                "HierarchyLabelFi: EDA Hierarchy 1 (label fi)",
                "Change: ADDED",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED when Current is missing a Hierarchy`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "DELETE from 'mHierarchy' WHERE HierarchyID = 300",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hierarchy")).containsExactly(
                "DomainCode: EDA",
                "HierarchyCode: EDA-H1",
                "HierarchyLabelFi: EDA Hierarchy 1 (label fi)",
                "Change: DELETED",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when DomainID is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mHierarchy' SET DomainID = NULL WHERE HierarchyID = 300",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Hierarchy")).containsExactly(
                "DomainCode: ",
                "HierarchyCode: EDA-H1",
                "HierarchyLabelFi: EDA Hierarchy 1 (label fi)",
                "Change: ADDED",
                "Notes: CURRENT ROW: #- Hierarchy Id: 300 #- Hierarchy Label: EDA Hierarchy 1",
                "-----------",
                "DomainCode: EDA",
                "HierarchyCode: EDA-H1",
                "HierarchyLabelFi: EDA Hierarchy 1 (label fi)",
                "Change: DELETED",
                "Notes: "
            )
        }
    }
}
