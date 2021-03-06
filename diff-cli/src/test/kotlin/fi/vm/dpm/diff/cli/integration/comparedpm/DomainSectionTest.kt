package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ Domain")
internal class DomainSectionTest : DpmDiffCliCompareTestBase(
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
    fun `Should provide identifying Note when reported Domain is having NULL ConceptID`() {
        executeDpmCompareForSectionAndExpectSuccess(
            identificationLabelLanguages = "fi,sv",
            baselineSql = "DELETE from 'mDomain' WHERE DomainID = 1",
            currentSql = "UPDATE 'mDomain' SET ConceptID = NULL WHERE DomainID = 1",
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

    @Test
    fun `Should report DUPLICATE_KEY_ALERT when Current has two Domains with same DomainCode`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql =
            """
                BEGIN;
                INSERT INTO 'mConcept' (
                    ConceptID,
                    ConceptType,
                    OwnerID,
                    CreationDate,
                    ModificationDate,
                    FromDate,
                    ToDate
                    )
                VALUES
                    (11, 'Domain', 1, NULL, NULL, NULL, NULL);


                INSERT INTO 'mConceptTranslation' (
                    ConceptID,
                    LanguageID,
                    Text,
                    Role
                    )
                VALUES
                    (11, 1, 'Duplicate domain A (label fi)', 'label');

                INSERT INTO 'mDomain' (
                    DomainID,
                    DomainCode,
                    DomainLabel,
                    DomainDescription,
                    DomainXBRLCode,
                    DataType,
                    IsTypedDomain,
                    ConceptID
                    )
                VALUES
                    (11, 'EDA', 'Duplicate domain A', NULL, NULL, NULL, 0, 11);

                COMMIT;
            """.trimIndent(),
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Domain")).containsExactly(
                "DomainCode: EDA",
                "DomainLabelFi: Explicit domain A (label fi)",
                "Change: DUPLICATE_KEY_ALERT",
                "IsTypedDomain: ",
                "IsTypedDomain(baseline): ",
                "TypedDomainDataType: ",
                "TypedDomainDataType(baseline): ",
                "Notes: CURRENT ROW: #- Domain Id: 1 #- Domain Label: Explicit domain A",
                "-----------",
                "DomainCode: EDA",
                "DomainLabelFi: Duplicate domain A (label fi)",
                "Change: DUPLICATE_KEY_ALERT",
                "IsTypedDomain: ",
                "IsTypedDomain(baseline): ",
                "TypedDomainDataType: ",
                "TypedDomainDataType(baseline): ",
                "Notes: CURRENT ROW: #- Domain Id: 11 #- Domain Label: Duplicate domain A"
            )
        }
    }
}
