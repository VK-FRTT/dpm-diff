package fi.vm.dpm.diff.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ Metric")
internal class CompareDpm_MetricSection_Test : DpmDiffCli_CompareTestBase(
    section = "Metric",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new Metric`() {
        executeDpmCompareForSectionAndExpectSuccess(

            baselineSql = "DELETE from 'mMetric' WHERE MetricID = 250",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: ADDED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED when Current is missing a Metric`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "DELETE from 'mMetric' WHERE MetricID = 250",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: DELETED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when DataType value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMetric' SET DataType = 'String' WHERE MetricID = 250",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: MODIFIED",
                "DataType: String",
                "DataType(baseline): Boolean",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: MODIFIED: #- Data Type"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when FlowType value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMetric' SET FlowType = 'Stock' WHERE MetricID = 250",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: MODIFIED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: Stock",
                "FlowType(baseline): Flow",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: MODIFIED: #- Flow Type"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when BalanceType value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMetric' SET BalanceType = 'Credit' WHERE MetricID = 250",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: MODIFIED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: Credit",
                "BalanceType(baseline): Debit",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: MODIFIED: #- Balance Type"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when ReferencedDomain value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMetric' SET ReferencedDomainID = 2 WHERE MetricID = 250",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: MODIFIED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: EDB",
                "ReferencedDomain(baseline): EDA",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: MODIFIED: #- Referenced Domain"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when ReferencedHierarchy value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMetric' SET ReferencedHierarchyID = 301 WHERE MetricID = 250",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: MODIFIED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: EDA-H2",
                "ReferencedHierarchy(baseline): EDA-H1",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: MODIFIED: #- Referenced Hierarchy"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when HierarchyStartingMember value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMetric' SET HierarchyStartingMemberID = 151 WHERE MetricID = 250",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: MODIFIED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: EDA-M2",
                "HierarchyStartingMember(baseline): EDA-M1",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: MODIFIED: #- Hierarchy Starting Member"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when IsStartingMemberIncluded value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMetric' SET IsStartingMemberIncluded = 1 WHERE MetricID = 250",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: MODIFIED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: 1",
                "IsStartingMemberIncluded(baseline): 0",
                "Notes: MODIFIED: #- Is Starting Member Included"
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when CorrespondingMemberID is NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMetric' SET CorrespondingMemberID = NULL WHERE MetricID = 250",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Metric")).containsExactly(
                "DomainCode: ",
                "MetricCode: ",
                "MetricLabelFi: ",
                "Change: ADDED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: CURRENT ROW: #- Metric Id: 250 #- Metric Label: null",
                "-----------",
                "DomainCode: EDA",
                "MetricCode: MET-M1",
                "MetricLabelFi: MET Member 1 (label fi)",
                "Change: DELETED",
                "DataType: ",
                "DataType(baseline): ",
                "FlowType: ",
                "FlowType(baseline): ",
                "BalanceType: ",
                "BalanceType(baseline): ",
                "ReferencedDomain: ",
                "ReferencedDomain(baseline): ",
                "ReferencedHierarchy: ",
                "ReferencedHierarchy(baseline): ",
                "HierarchyStartingMember: ",
                "HierarchyStartingMember(baseline): ",
                "IsStartingMemberIncluded: ",
                "IsStartingMemberIncluded(baseline): ",
                "Notes: "
            )
        }
    }
}
