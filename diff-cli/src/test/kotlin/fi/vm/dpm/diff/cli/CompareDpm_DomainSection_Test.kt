package fi.vm.dpm.diff.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ Domain")
internal class CompareDpmDomainSectionTest : DpmDiffCli_CompareTestBase(
    section = "Domain",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new Domain`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mDomain' WHERE DomainID = 1",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Domain")).containsExactly(
                "DomainCode: EDA",
                "DomainLabelFi: Explicit domain A (label fi)",
                "Change: ADDED",
                "IsTypedDomain: ",
                "IsTypedDomain(baseline): ",
                "TypedDomainDataType: ",
                "TypedDomainDataType(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report DELETED when Current is missing a Domain`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "DELETE from 'mDomain' WHERE DomainID = 50",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Domain")).containsExactly(
                "DomainCode: TDS",
                "DomainLabelFi: Typed domain S (label fi)",
                "Change: DELETED",
                "IsTypedDomain: ",
                "IsTypedDomain(baseline): ",
                "TypedDomainDataType: ",
                "TypedDomainDataType(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when DomainCode is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mDomain' SET DomainCode = NULL WHERE DomainID = 1",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Domain")).containsExactly(
                "DomainCode: ",
                "DomainLabelFi: Explicit domain A (label fi)",
                "Change: ADDED",
                "IsTypedDomain: ",
                "IsTypedDomain(baseline): ",
                "TypedDomainDataType: ",
                "TypedDomainDataType(baseline): ",
                "Notes: CURRENT ROW: #- Domain Id: 1 #- Domain Label: Explicit domain A",
                "-----------",
                "DomainCode: EDA",
                "DomainLabelFi: Explicit domain A (label fi)",
                "Change: DELETED",
                "IsTypedDomain: ",
                "IsTypedDomain(baseline): ",
                "TypedDomainDataType: ",
                "TypedDomainDataType(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when DataType value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mDomain' SET DataType = 'Integer' WHERE DomainID = 50",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Domain")).containsExactly(
                "DomainCode: TDS",
                "DomainLabelFi: Typed domain S (label fi)",
                "Change: MODIFIED",
                "IsTypedDomain: ",
                "IsTypedDomain(baseline): ",
                "TypedDomainDataType: Integer",
                "TypedDomainDataType(baseline): String",
                "Notes: MODIFIED: #- Typed Domain Data Type"
            )
        }
    }

    @Test
    fun `Should report MODIFIED when IsTypedDomain value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mDomain' SET IsTypedDomain = 1 WHERE DomainID = 1",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Domain")).containsExactly(
                "DomainCode: EDA",
                "DomainLabelFi: Explicit domain A (label fi)",
                "Change: MODIFIED",
                "IsTypedDomain: 1",
                "IsTypedDomain(baseline): 0",
                "TypedDomainDataType: ",
                "TypedDomainDataType(baseline): ",
                "Notes: MODIFIED: #- Is Typed Domain"
            )
        }
    }

    @Test
    fun `Should provide identifying Note when reported Domain is not having Label translation for any IdentificationLabelLanguage`() {
        executeDpmCompareForSectionAndExpectSuccess(
            identificationLabelLanguages = "fi,sv",
            baselineSql = "DELETE from 'mDomain' WHERE DomainID = 1",
            currentSql = "DELETE from 'mConceptTranslation' WHERE ConceptID = 1",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Domain")).containsExactly(
                "DomainCode: EDA",
                "DomainLabelFi: ",
                "DomainLabelSv: ",
                "Change: ADDED",
                "IsTypedDomain: ",
                "IsTypedDomain(baseline): ",
                "TypedDomainDataType: ",
                "TypedDomainDataType(baseline): ",
                "Notes: CURRENT ROW: #- Domain Id: 1 #- Domain Label: Explicit domain A"
            )
        }
    }

    @Test
    fun `Should not provide identifying Note when reported Domain is having Label translation at least for one IdentificationLabelLanguage`() {
        executeDpmCompareForSectionAndExpectSuccess(
            identificationLabelLanguages = "fi,sv",
            baselineSql = "DELETE from 'mDomain' WHERE DomainID = 1",
            currentSql = "DELETE from 'mConceptTranslation' WHERE ConceptID = 1 AND LanguageID != 2",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Domain")).containsExactly(
                "DomainCode: EDA",
                "DomainLabelFi: ",
                "DomainLabelSv: Explicit domain A (label sv)",
                "Change: ADDED",
                "IsTypedDomain: ",
                "IsTypedDomain(baseline): ",
                "TypedDomainDataType: ",
                "TypedDomainDataType(baseline): ",
                "Notes: "
            )
        }
    }
}
