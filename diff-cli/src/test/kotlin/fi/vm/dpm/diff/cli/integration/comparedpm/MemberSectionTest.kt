package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ Member")
internal class MemberSectionTest : DpmDiffCliCompareTestBase(
    section = "Member",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new Member`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mMember' WHERE MemberID = 150",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Member")).containsExactly(
                "DomainCode: EDA",
                "MemberCode: EDA-M1",
                "MemberLabelFi: EDA Member 1 (label fi)",
                "Change: ADDED",
                "IsDefaultMember: ",
                "IsDefaultMember(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED when Current is missing a Member`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "DELETE from 'mMember' WHERE MemberID = 150",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Member")).containsExactly(
                "DomainCode: EDA",
                "MemberCode: EDA-M1",
                "MemberLabelFi: EDA Member 1 (label fi)",
                "Change: DELETED",
                "IsDefaultMember: ",
                "IsDefaultMember(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when MemberCode is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMember' SET MemberCode = NULL WHERE MemberID = 150",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Member")).containsExactly(
                "DomainCode: EDA",
                "MemberCode: ",
                "MemberLabelFi: EDA Member 1 (label fi)",
                "Change: ADDED",
                "IsDefaultMember: ",
                "IsDefaultMember(baseline): ",
                "Notes: CURRENT ROW: #- Member Id: 150 #- Member Label: EDA Member 1",
                "-----------",
                "DomainCode: EDA",
                "MemberCode: EDA-M1",
                "MemberLabelFi: EDA Member 1 (label fi)",
                "Change: DELETED",
                "IsDefaultMember: ",
                "IsDefaultMember(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED & ADDED when Domain Reference is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMember' SET DomainID = 2 WHERE MemberID = 150",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Member")).containsExactly(
                "DomainCode: EDA",
                "MemberCode: EDA-M1",
                "MemberLabelFi: EDA Member 1 (label fi)",
                "Change: DELETED",
                "IsDefaultMember: ",
                "IsDefaultMember(baseline): ",
                "Notes: ",
                "-----------",
                "DomainCode: EDB",
                "MemberCode: EDA-M1",
                "MemberLabelFi: EDA Member 1 (label fi)",
                "Change: ADDED",
                "IsDefaultMember: ",
                "IsDefaultMember(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when IsDefaultMember value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mMember' SET IsDefaultMember = 1 WHERE MemberID = 150",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Member")).containsExactly(
                "DomainCode: EDA",
                "MemberCode: EDA-M1",
                "MemberLabelFi: EDA Member 1 (label fi)",
                "Change: MODIFIED",
                "IsDefaultMember: 1",
                "IsDefaultMember(baseline): 0",
                "Notes: MODIFIED: #- Is Default Member"
            )
        }
    }
}
