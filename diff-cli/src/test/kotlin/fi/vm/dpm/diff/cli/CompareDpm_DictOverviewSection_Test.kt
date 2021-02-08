package fi.vm.dpm.diff.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ DictOverview")
internal class CompareDpm_DictOverviewSection_Test : DpmDiffCli_CompareTestBase(
    section = "DictOverview",
    commonSetupSql = compareDpmSetupSql()
    ) {

    @Nested
    inner class AddedElements {

        @Test
        fun `Should report ADDED when Current has a new Domain`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql = "DELETE from 'mDomain' WHERE DomainID = 2",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Member`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql = "DELETE from 'mMember' WHERE MemberID = 151",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Member",
                    "ElementCode: EDA-M2",
                    "ElementLabelFi: EDA Member 2 (label fi)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Metric`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql =
                """
                    BEGIN;
                    DELETE from 'mMetric' WHERE MetricID = 250;
                    DELETE from 'mMember' WHERE MemberID = 200;
                    COMMIT;
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Metric",
                    "ElementCode: MET-M1",
                    "ElementLabelFi: MET Member 1 (label fi)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Dimension`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql = "DELETE from 'mDimension' WHERE DimensionID = 400",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Dimension",
                    "ElementCode: EDA-DIM",
                    "ElementLabelFi: EDA Dimension (label fi)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Hierarchy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql = "DELETE from 'mHierarchy' WHERE HierarchyID = 301",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Hierarchy",
                    "ElementCode: EDA-H2",
                    "ElementLabelFi: EDA Hierarchy 2 (label fi)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class DeletedElements {

        @Test
        fun `Should report DELETED when Current is missing a Domain`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql = "DELETE from 'mDomain' WHERE DomainID = 2",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: Explicit domain B (label fi)",
                    "Change: DELETED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Member`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql = "DELETE from 'mMember' WHERE MemberID = 151",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Member",
                    "ElementCode: EDA-M2",
                    "ElementLabelFi: EDA Member 2 (label fi)",
                    "Change: DELETED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Metric`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql =
                """
                    BEGIN;
                    DELETE from 'mMetric' WHERE MetricID = 250;
                    DELETE from 'mMember' WHERE MemberID = 200;
                    COMMIT;
                """.trimIndent(),
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Metric",
                    "ElementCode: MET-M1",
                    "ElementLabelFi: MET Member 1 (label fi)",
                    "Change: DELETED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Dimension`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql = "DELETE from 'mDimension' WHERE DimensionID = 400",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Dimension",
                    "ElementCode: EDA-DIM",
                    "ElementLabelFi: EDA Dimension (label fi)",
                    "Change: DELETED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Hierarchy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql = "DELETE from 'mHierarchy' WHERE HierarchyID = 301",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: Domain",
                    "ParentElementCode: EDA",
                    "ElementType: Hierarchy",
                    "ElementCode: EDA-H2",
                    "ElementLabelFi: EDA Hierarchy 2 (label fi)",
                    "Change: DELETED",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class IdentificationNote {

        @Test
        fun `Should provide identifying Note when reported Domain is not having Label translation for any IdentificationLabelLanguage`() {
            executeDpmCompareForSectionAndExpectSuccess(
                identificationLabelLanguages = "fi,sv",
                baselineSql = "DELETE from 'mDomain' WHERE DomainID = 2",
                currentSql = "DELETE from 'mConceptTranslation' WHERE ConceptID = 2",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: ",
                    "ElementLabelSv: ",
                    "Change: ADDED",
                    "Notes: CURRENT ROW: #- Element Id: 2 #- Element Label: Explicit domain B"
                )
            }
        }

        @Test
        fun `Should not provide identifying Note when reported Domain is having Label translation at least for one IdentificationLabelLanguage`() {
            executeDpmCompareForSectionAndExpectSuccess(
                identificationLabelLanguages = "fi,sv",
                baselineSql = "DELETE from 'mDomain' WHERE DomainID = 2",
                currentSql = "DELETE from 'mConceptTranslation' WHERE ConceptID = 2 AND LanguageID != 2",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: Domain",
                    "ElementCode: EDB",
                    "ElementLabelFi: ",
                    "ElementLabelSv: Explicit domain B (label sv)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }
    }

    @Test
    fun `Should report ADDED & DELETED when DomainCode is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mDomain' SET DomainCode = NULL WHERE DomainID = 2",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Dict_Overview")).containsExactly(
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: Domain",
                "ElementCode: ",
                "ElementLabelFi: Explicit domain B (label fi)",
                "Change: ADDED",
                "Notes: CURRENT ROW: #- Element Id: 2 #- Element Label: Explicit domain B",
                "-----------",
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: Domain",
                "ElementCode: EDB",
                "ElementLabelFi: Explicit domain B (label fi)",
                "Change: DELETED",
                "Notes: "
            )
        }
    }
}
