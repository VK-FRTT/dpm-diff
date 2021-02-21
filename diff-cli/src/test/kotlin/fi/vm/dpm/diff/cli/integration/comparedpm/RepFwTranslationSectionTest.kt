package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ RepFwTranslation")
internal class RepFwTranslationSectionTest : DpmDiffCliCompareTestBase(
    section = "RepFwTranslation",
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
                    WHERE ConceptID = 500 AND Role = 'label'
                """.trimIndent(),

                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 500 AND LanguageID IN (2,3,4) AND Role = 'label'
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: RFA framework (label fi)",
                    "TranslationRole: label",
                    "Language: fi",
                    "Change: ADDED",
                    "Translation: RFA framework (label fi)",
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
                    WHERE ConceptID = 500 AND LanguageID IN (2,3,4) AND Role = 'label'
                """.trimIndent(),

                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 500 AND Role = 'label'
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: RFA framework (label fi)",
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
        fun `Should report ADDED when Current has a new Label SV translation for ReportingFramework`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 500 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: RFA framework (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: RFA framework (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Label SV translation for Taxonomy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 600 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ReportingFramework",
                    "ParentElementCode: RFA",
                    "ElementType: Taxonomy",
                    "ElementCode: TXA",
                    "ElementLabelFi: TXA taxonomy (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: TXA taxonomy (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Label SV translation for Module`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 700 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Module",
                    "ElementCode: MDA",
                    "ElementLabelFi: MDA module (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: MDA module (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Label SV translation for Table`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 800 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Table",
                    "ElementCode: TBA",
                    "ElementLabelFi: TBA table (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: TBA table (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class DeletedTranslations {

        @Test
        fun `Should report DELETED when Current is missing Label SV translation for ReportingFramework`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 500 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: RFA framework (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: DELETED",
                    "Translation: ",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Label SV translation for Taxonomy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 600 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ReportingFramework",
                    "ParentElementCode: RFA",
                    "ElementType: Taxonomy",
                    "ElementCode: TXA",
                    "ElementLabelFi: TXA taxonomy (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: DELETED",
                    "Translation: ",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Label SV translation for Module`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 700 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Module",
                    "ElementCode: MDA",
                    "ElementLabelFi: MDA module (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: DELETED",
                    "Translation: ",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Label SV translation for Table`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 800 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Table",
                    "ElementCode: TBA",
                    "ElementLabelFi: TBA table (label fi)",
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
        fun `Should report MODIFIED when ConceptTranslation Text value is changed for ReportingFramework`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    UPDATE 'mConceptTranslation' SET Text = 'sv label from test'
                    WHERE ConceptID = 500 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: RFA framework (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): RFA framework (label sv)",
                    "Notes: MODIFIED: #- Translation"
                )
            }
        }

        @Test
        fun `Should report MODIFIED when ConceptTranslation Text value is changed for Taxonomy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    UPDATE 'mConceptTranslation' SET Text = 'sv label from test'
                    WHERE ConceptID = 600 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ReportingFramework",
                    "ParentElementCode: RFA",
                    "ElementType: Taxonomy",
                    "ElementCode: TXA",
                    "ElementLabelFi: TXA taxonomy (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): TXA taxonomy (label sv)",
                    "Notes: MODIFIED: #- Translation"
                )
            }
        }

        @Test
        fun `Should report MODIFIED when ConceptTranslation Text value is changed for Module`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    UPDATE 'mConceptTranslation' SET Text = 'sv label from test'
                    WHERE ConceptID = 700 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Module",
                    "ElementCode: MDA",
                    "ElementLabelFi: MDA module (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): MDA module (label sv)",
                    "Notes: MODIFIED: #- Translation"
                )
            }
        }

        @Test
        fun `Should report MODIFIED when ConceptTranslation Text value is changed for Table`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    UPDATE 'mConceptTranslation' SET Text = 'sv label from test'
                    WHERE ConceptID = 800 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Table",
                    "ElementCode: TBA",
                    "ElementLabelFi: TBA table (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): TBA table (label sv)",
                    "Notes: MODIFIED: #- Translation"
                )
            }
        }
    }

    @Nested
    inner class IdentificationNote {

        @Test
        fun `Should provide identifying Note when reported Element is not having Label translation for any IdentificationLabelLanguage`() {
            executeDpmCompareForSectionAndExpectSuccess(
                identificationLabelLanguages = "fi,sv",
                translationLanguages = "en,pl",

                // EN Label to be reported as new one
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 500 AND LanguageID = 3 AND Role = 'label'
                """.trimIndent(),

                // FI / SV identification labels to be missing
                currentSql =
                """
                    BEGIN;

                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 500 AND LanguageID = 1 AND Role = 'label';

                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 500 AND LanguageID = 2 AND Role = 'label';

                    COMMIT;
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: ",
                    "ElementLabelSv: ",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: RFA framework (label en)",
                    "Translation(baseline): ",
                    "Notes: CURRENT ROW: #- Element Id: 500 #- Element Label: RFA framework"
                )
            }
        }

        @Test
        fun `Should not provide identifying Note when reported Element is having Label translation at least for one IdentificationLabelLanguage`() {
            executeDpmCompareForSectionAndExpectSuccess(
                identificationLabelLanguages = "fi,sv",
                translationLanguages = "en,pl",

                // EN Label to be reported as new one
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 500 AND LanguageID = 3 AND Role = 'label'
                """.trimIndent(),

                // SV identification label to be missing
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 500 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: RFA framework (label fi)",
                    "ElementLabelSv: ",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: RFA framework (label en)",
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
                    WHERE ConceptID = 500 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 2
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: RFA framework (label fi)",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: RFA framework (label en)",
                    "Translation(baseline): ",
                    "Notes: ",
                    "-----------",
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: RFA framework (label fi)",
                    "TranslationRole: label",
                    "Language: pl",
                    "Change: ADDED",
                    "Translation: RFA framework (label pl)",
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
                    WHERE ConceptID = 500 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFA",
                    "ElementLabelFi: RFA framework (label fi)",
                    "ElementLabelSv: RFA framework (label sv)",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: RFA framework (label en)",
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
                    (500, 2, 'RFA framework (description sv)', 'description'),
                    (500, 2, 'RFA framework (custom-role sv)', 'custom-role'),
                    (500, 2, 'RFA framework (null-role sv)', null);
            """.trimIndent(),

            expectedChanges = 3
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Translation")).containsExactly(
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: ReportingFramework",
                "ElementCode: RFA",
                "ElementLabelFi: RFA framework (label fi)",
                "TranslationRole: ",
                "Language: sv",
                "Change: ADDED",
                "Translation: RFA framework (null-role sv)",
                "Translation(baseline): ",
                "Notes: ",
                "-----------",
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: ReportingFramework",
                "ElementCode: RFA",
                "ElementLabelFi: RFA framework (label fi)",
                "TranslationRole: description",
                "Language: sv",
                "Change: ADDED",
                "Translation: RFA framework (description sv)",
                "Translation(baseline): ",
                "Notes: ",
                "-----------",
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: ReportingFramework",
                "ElementCode: RFA",
                "ElementLabelFi: RFA framework (label fi)",
                "TranslationRole: custom-role",
                "Language: sv",
                "Change: ADDED",
                "Translation: RFA framework (custom-role sv)",
                "Translation(baseline): ",
                "Notes: "
            )
        }
    }
}
