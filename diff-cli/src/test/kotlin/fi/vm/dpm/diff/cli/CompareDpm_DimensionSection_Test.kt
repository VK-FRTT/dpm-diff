package fi.vm.dpm.diff.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ Dimension")
internal class CompareDpm_DimensionSection_Test : DpmDiffCli_CompareTestBase(
    section = "Dimension",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new Dimension`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mDimension' WHERE DimensionID = 400",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Dimension")).containsExactly(
                "DimensionCode: EDA-DIM",
                "DimensionLabelFi: EDA Dimension (label fi)",
                "Change: ADDED",
                "ReferencedDomainCode: ",
                "ReferencedDomainCode(baseline): ",
                "IsTypedDimension: ",
                "IsTypedDimension(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED when Current is missing a Dimension`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "DELETE from 'mDimension' WHERE DimensionID = 450",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Dimension")).containsExactly(
                "DimensionCode: TDS-DIM",
                "DimensionLabelFi: TDS Dimension (label fi)",
                "Change: DELETED",
                "ReferencedDomainCode: ",
                "ReferencedDomainCode(baseline): ",
                "IsTypedDimension: ",
                "IsTypedDimension(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when DimensionCode is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mDimension' SET DimensionCode = NULL WHERE DimensionID = 400",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Dimension")).containsExactly(
                "DimensionCode: ",
                "DimensionLabelFi: EDA Dimension (label fi)",
                "Change: ADDED",
                "ReferencedDomainCode: ",
                "ReferencedDomainCode(baseline): ",
                "IsTypedDimension: ",
                "IsTypedDimension(baseline): ",
                "Notes: CURRENT ROW: #- Dimension Id: 400 #- Dimension Label: EDA dimension",
                "-----------",
                "DimensionCode: EDA-DIM",
                "DimensionLabelFi: EDA Dimension (label fi)",
                "Change: DELETED",
                "ReferencedDomainCode: ",
                "ReferencedDomainCode(baseline): ",
                "IsTypedDimension: ",
                "IsTypedDimension(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when Domain Reference is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mDimension' SET DomainID = 50 WHERE DimensionID = 400",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Dimension")).containsExactly(
                "DimensionCode: EDA-DIM",
                "DimensionLabelFi: EDA Dimension (label fi)",
                "Change: MODIFIED",
                "ReferencedDomainCode: TDS",
                "ReferencedDomainCode(baseline): EDA",
                "IsTypedDimension: ",
                "IsTypedDimension(baseline): ",
                "Notes: MODIFIED: #- Referenced Domain Code"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when IsTypedDimension value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mDimension' SET IsTypedDimension = 1 WHERE DimensionID = 400",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Dimension")).containsExactly(
                "DimensionCode: EDA-DIM",
                "DimensionLabelFi: EDA Dimension (label fi)",
                "Change: MODIFIED",
                "ReferencedDomainCode: ",
                "ReferencedDomainCode(baseline): ",
                "IsTypedDimension: 1",
                "IsTypedDimension(baseline): 0",
                "Notes: MODIFIED: #- Is Typed Dimension"
            )
        }
    }
}
