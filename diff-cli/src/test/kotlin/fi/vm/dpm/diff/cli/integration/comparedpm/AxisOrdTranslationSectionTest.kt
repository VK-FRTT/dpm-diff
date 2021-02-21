package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ AxisOrdTranslation")
internal class AxisOrdTranslationSectionTest : DpmDiffCliCompareTestBase(
    section = "AxisOrdTranslation",
    commonSetupSql = compareDpmSetupSql()
) {

    @Nested
    inner class TranslationAmountEdgeCases {

        @Test
        fun `Should report ADDED when first translation is added`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND Role = 'label'
                """.trimIndent(),

                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND LanguageID IN (2,3,4) AND Role = 'label'
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: AOB axis ordinate (label fi)",
                    "TranslationRole: label",
                    "Language: fi",
                    "Change: ADDED",
                    "Translation: AOB axis ordinate (label fi)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when last translation is removed`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND LanguageID IN (2,3,4) AND Role = 'label'
                """.trimIndent(),

                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND Role = 'label'
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: AOB axis ordinate (label fi)",
                    "TranslationRole: label",
                    "Language: fi",
                    "Change: DELETED",
                    "Translation: ",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class AddedTranslations {

        @Test
        fun `Should report ADDED when Current has a new Label SV translation for AxisOrdinate`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: AOB axis ordinate (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: AOB axis ordinate (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class DeletedTranslations {

        @Test
        fun `Should report DELETED when Current is missing Label SV translation for AxisOrdinate`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: AOB axis ordinate (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: DELETED",
                    "Translation: ",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class ModifiedTranslations {

        @Test
        fun `Should report MODIFIED when ConceptTranslation Text value is changed for Domain`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    UPDATE 'mConceptTranslation' SET Text = 'sv label from test'
                    WHERE ConceptID = 1101 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: AOB axis ordinate (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): AOB axis ordinate (label sv)",
                    "Notes: MODIFIED: #- Translation"
                )
            }
        }
    }

    @Nested
    inner class IdentificationNote {

        @Test
        fun `Should provide identifying Note when reported AxisOrdinate is not having Label translation for any IdentificationLabelLanguage`() {
            executeDpmCompareForSectionAndExpectSuccess(
                identificationLabelLanguages = "fi,sv",
                translationLanguages = "en,pl",

                // EN Label to be reported as new one
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND LanguageID = 3 AND Role = 'label'
                """.trimIndent(),

                // FI / SV identification labels to be missing
                currentSql =
                """
                    BEGIN;

                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND LanguageID = 1 AND Role = 'label';

                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND LanguageID = 2 AND Role = 'label';

                    COMMIT;
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: ",
                    "OrdinateLabelSv: ",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: AOB axis ordinate (label en)",
                    "Translation(baseline): ",
                    "Notes: CURRENT ROW: #- Ordinate Id: 1101 #- Ordinate Label: AOB axis ordinate"
                )
            }
        }

        @Test
        fun `Should not provide identifying Note when reported AxisOrdinate is having Label translation at least for one IdentificationLabelLanguage`() {
            executeDpmCompareForSectionAndExpectSuccess(
                identificationLabelLanguages = "fi,sv",
                translationLanguages = "en,pl",

                // EN Label to be reported as new one
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND LanguageID = 3 AND Role = 'label'
                """.trimIndent(),

                // FI / SV identification labels to be missing
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: AOB axis ordinate (label fi)",
                    "OrdinateLabelSv: ",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: AOB axis ordinate (label en)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class IncludedTranslationLanguagesOption {

        @Test
        fun `Should report ADDED only for requested translation languages`() {
            executeDpmCompareForSectionAndExpectSuccess(
                translationLanguages = "en,pl",
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 2
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: AOB axis ordinate (label fi)",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: AOB axis ordinate (label en)",
                    "Translation(baseline): ",
                    "Notes: ",
                    "-----------",
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: AOB axis ordinate (label fi)",
                    "TranslationRole: label",
                    "Language: pl",
                    "Change: ADDED",
                    "Translation: AOB axis ordinate (label pl)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED only for requested single translation language`() {
            executeDpmCompareForSectionAndExpectSuccess(
                identificationLabelLanguages = "fi,sv",
                translationLanguages = "en",
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 1101 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                    "TaxonomyCode: TXA",
                    "TableCode: TBA",
                    "AxisOrientation: X",
                    "OrdinateCode: AOB",
                    "OrdinateLabelFi: AOB axis ordinate (label fi)",
                    "OrdinateLabelSv: AOB axis ordinate (label sv)",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: AOB axis ordinate (label en)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }
    }

    @Test
    fun `Should report ADDED also for other translation roles than label`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql =
            """
                INSERT INTO 'mConceptTranslation' (
                    ConceptID,
                    LanguageID,
                    Text,
                    Role
                    )
                VALUES
                    (1101, 2, 'AOB axis ordinate (description sv)', 'description'),
                    (1101, 2, 'AOB axis ordinate (custom-role sv)', 'custom-role'),
                    (1101, 2, 'AOB axis ordinate (null-role sv)', null);
            """.trimIndent(),

            expectedChanges = 3
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Axis_Ord_Translation")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "TranslationRole: ",
                "Language: sv",
                "Change: ADDED",
                "Translation: AOB axis ordinate (null-role sv)",
                "Translation(baseline): ",
                "Notes: ",
                "-----------",
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "TranslationRole: description",
                "Language: sv",
                "Change: ADDED",
                "Translation: AOB axis ordinate (description sv)",
                "Translation(baseline): ",
                "Notes: ",
                "-----------",
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "AxisOrientation: X",
                "OrdinateCode: AOB",
                "OrdinateLabelFi: AOB axis ordinate (label fi)",
                "TranslationRole: custom-role",
                "Language: sv",
                "Change: ADDED",
                "Translation: AOB axis ordinate (custom-role sv)",
                "Translation(baseline): ",
                "Notes: "
            )
        }
    }
}
