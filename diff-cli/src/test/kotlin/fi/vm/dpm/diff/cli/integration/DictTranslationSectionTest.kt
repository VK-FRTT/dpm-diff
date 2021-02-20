package fi.vm.dpm.diff.cli.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ DictTranslation")
internal class DictTranslationSectionTest : DpmDiffCliCompareTestBase(
    section = "DictTranslation",
    commonSetupSql = compareDpmSetupSql()
) {

    @Nested
    inner class NoPreviousTranslations {

        @Test
        fun `Should report ADDED when first translation is added`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 2 AND Role = 'label'
                """.trimIndent(),

                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 2 AND LanguageID IN (2,3,4) AND Role = 'label'
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
                    "TranslationRole: label",
                    "Language: fi",
                    "Change: ADDED",
                    "Translation: Explicit domain B (label fi)",
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
                    WHERE ConceptID = 2 AND LanguageID IN (2,3,4) AND Role = 'label'
                """.trimIndent(),

                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 2 AND Role = 'label'
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
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
        fun `Should report ADDED when Current has a new Label SV translation for Domain`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 2 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: Explicit domain B (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Label SV translation for Member`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 151 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Member",
                    "ElementCode: EDA-M2",
                    "ElementLabelFi: EDA Member 2 (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: EDA Member 2 (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Label SV translation for Metric`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 200 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Metric",
                    "ElementCode: MET-M1",
                    "ElementLabelFi: MET Member 1 (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: MET Member 1 (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Label SV translation for Dimension`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 400 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Dimension",
                    "ElementCode: EDA-DIM",
                    "ElementLabelFi: EDA Dimension (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: EDA Dimension (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Label SV translation for Hierarchy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 301 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Hierarchy",
                    "ElementCode: EDA-H2",
                    "ElementLabelFi: EDA Hierarchy 2 (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: ADDED",
                    "Translation: EDA Hierarchy 2 (label sv)",
                    "Translation(baseline): ",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class DeletedTranslations {

        @Test
        fun `Should report DELETED when Current is missing Label SV translation for Domain`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 2 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
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
        fun `Should report DELETED when Current is missing a Label SV translation for Member`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 151 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Member",
                    "ElementCode: EDA-M2",
                    "ElementLabelFi: EDA Member 2 (label fi)",
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
        fun `Should report DELETED when Current is missing a Label SV translation for Metric`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 200 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Metric",
                    "ElementCode: MET-M1",
                    "ElementLabelFi: MET Member 1 (label fi)",
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
        fun `Should report DELETED when Current is missing a Label SV translation for Dimension`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 400 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Dimension",
                    "ElementCode: EDA-DIM",
                    "ElementLabelFi: EDA Dimension (label fi)",
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
        fun `Should report DELETED when Current is missing a Label SV translation for Hierarchy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 301 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Hierarchy",
                    "ElementCode: EDA-H2",
                    "ElementLabelFi: EDA Hierarchy 2 (label fi)",
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
                    WHERE ConceptID = 2 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): Explicit domain B (label sv)",
                    "Notes: MODIFIED: #- Translation"
                )
            }
        }

        @Test
        fun `Should report MODIFIED when ConceptTranslation Text value is changed for Member`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    UPDATE 'mConceptTranslation' SET Text = 'sv label from test'
                    WHERE ConceptID = 151 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Member",
                    "ElementCode: EDA-M2",
                    "ElementLabelFi: EDA Member 2 (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): EDA Member 2 (label sv)",
                    "Notes: MODIFIED: #- Translation"
                )
            }
        }

        @Test
        fun `Should report MODIFIED when ConceptTranslation Text value is changed for Metric`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    UPDATE 'mConceptTranslation' SET Text = 'sv label from test'
                    WHERE ConceptID = 200 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Metric",
                    "ElementCode: MET-M1",
                    "ElementLabelFi: MET Member 1 (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): MET Member 1 (label sv)",
                    "Notes: MODIFIED: #- Translation"
                )
            }
        }

        @Test
        fun `Should report MODIFIED when ConceptTranslation Text value is changed for Dimension`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    UPDATE 'mConceptTranslation' SET Text = 'sv label from test'
                    WHERE ConceptID = 400 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Dimension",
                    "ElementCode: EDA-DIM",
                    "ElementLabelFi: EDA Dimension (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): EDA Dimension (label sv)",
                    "Notes: MODIFIED: #- Translation"
                )
            }
        }

        @Test
        fun `Should report MODIFIED when ConceptTranslation Text value is changed for Hierarchy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    UPDATE 'mConceptTranslation' SET Text = 'sv label from test'
                    WHERE ConceptID = 301 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Hierarchy",
                    "ElementCode: EDA-H2",
                    "ElementLabelFi: EDA Hierarchy 2 (label fi)",
                    "TranslationRole: label",
                    "Language: sv",
                    "Change: MODIFIED",
                    "Translation: sv label from test",
                    "Translation(baseline): EDA Hierarchy 2 (label sv)",
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
                    WHERE ConceptID = 2 AND LanguageID = 3 AND Role = 'label'
                """.trimIndent(),

                // FI / SV identification labels to be missing
                currentSql =
                """
                    BEGIN;

                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 2 AND LanguageID = 1 AND Role = 'label';

                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 2 AND LanguageID = 2 AND Role = 'label';

                    COMMIT;
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: ",
                    "ElementLabelSv: ",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: Explicit domain B (label en)",
                    "Translation(baseline): ",
                    "Notes: CURRENT ROW: #- Element Id: 2 #- Element Label: Explicit domain B"
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
                    WHERE ConceptID = 2 AND LanguageID = 3 AND Role = 'label'
                """.trimIndent(),

                // FI / SV identification labels to be missing
                currentSql =
                """
                    DELETE from 'mConceptTranslation'
                    WHERE ConceptID = 2 AND LanguageID = 2 AND Role = 'label'
                """.trimIndent(),

                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
                    "ElementLabelSv: ",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: Explicit domain B (label en)",
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
                    WHERE ConceptID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 2
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: Explicit domain B (label en)",
                    "Translation(baseline): ",
                    "Notes: ",
                    "-----------",
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
                    "TranslationRole: label",
                    "Language: pl",
                    "Change: ADDED",
                    "Translation: Explicit domain B (label pl)",
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
                    WHERE ConceptID = 2 AND Role = 'label'
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
                    "ElementLabelSv: Explicit domain B (label sv)",
                    "TranslationRole: label",
                    "Language: en",
                    "Change: ADDED",
                    "Translation: Explicit domain B (label en)",
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
                    (2, 2, 'Explicit domain A (description sv)', 'description'),
                    (2, 2, 'Explicit domain A (custom-role sv)', 'custom-role'),
                    (2, 2, 'Explicit domain A (null-role sv)', null);
            """.trimIndent(),

            expectedChanges = 3
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Translation")).containsExactly(
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: Domain",
                "ElementCode: EDB",
                "ElementLabelFi: Explicit domain B (label fi)",
                "TranslationRole: ",
                "Language: sv",
                "Change: ADDED",
                "Translation: Explicit domain A (null-role sv)",
                "Translation(baseline): ",
                "Notes: ",
                "-----------",
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: Domain",
                "ElementCode: EDB",
                "ElementLabelFi: Explicit domain B (label fi)",
                "TranslationRole: description",
                "Language: sv",
                "Change: ADDED",
                "Translation: Explicit domain A (description sv)",
                "Translation(baseline): ",
                "Notes: ",
                "-----------",
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: Domain",
                "ElementCode: EDB",
                "ElementLabelFi: Explicit domain B (label fi)",
                "TranslationRole: custom-role",
                "Language: sv",
                "Change: ADDED",
                "Translation: Explicit domain A (custom-role sv)",
                "Translation(baseline): ",
                "Notes: "
            )
        }
    }
}
