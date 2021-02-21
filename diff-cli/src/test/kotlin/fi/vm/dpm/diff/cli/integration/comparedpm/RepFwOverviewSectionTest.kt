package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ RepFwOverview")
internal class RepFwOverviewSectionTest : DpmDiffCliCompareTestBase(
    section = "RepFwOverview",
    commonSetupSql = compareDpmSetupSql()
) {

    @Nested
    inner class AddedElements {

        @Test
        fun `Should report ADDED when Current has a new ReportingFramework`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql = "DELETE from 'mReportingFramework' WHERE FrameworkID = 501",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFB",
                    "ElementLabelFi: RFB framework (label fi)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Taxonomy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql = "DELETE from 'mTaxonomy' WHERE TaxonomyID = 601",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: ReportingFramework",
                    "ParentElementCode: RFA",
                    "ElementType: Taxonomy",
                    "ElementCode: TXB",
                    "ElementLabelFi: TXB taxonomy (label fi)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Module`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql = "DELETE from 'mModule' WHERE ModuleID = 700",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Module",
                    "ElementCode: MDA",
                    "ElementLabelFi: MDA module (label fi)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report ADDED when Current has a new Table`() {
            executeDpmCompareForSectionAndExpectSuccess(
                baselineSql = "DELETE from 'mTable' WHERE TableID = 800",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Table",
                    "ElementCode: TBA",
                    "ElementLabelFi: TBA table (label fi)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class DeletedElements {

        @Test
        fun `Should report DELETED when Current is missing a ReportingFramework`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql = "DELETE from 'mReportingFramework' WHERE FrameworkID = 501",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFB",
                    "ElementLabelFi: RFB framework (label fi)",
                    "Change: DELETED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Taxonomy`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql = "DELETE from 'mTaxonomy' WHERE TaxonomyID = 601",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: ReportingFramework",
                    "ParentElementCode: RFA",
                    "ElementType: Taxonomy",
                    "ElementCode: TXB",
                    "ElementLabelFi: TXB taxonomy (label fi)",
                    "Change: DELETED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Module`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql = "DELETE from 'mModule' WHERE ModuleID = 700",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Module",
                    "ElementCode: MDA",
                    "ElementLabelFi: MDA module (label fi)",
                    "Change: DELETED",
                    "Notes: "
                )
            }
        }

        @Test
        fun `Should report DELETED when Current is missing a Table`() {
            executeDpmCompareForSectionAndExpectSuccess(
                currentSql = "DELETE from 'mTable' WHERE TableID = 800",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: Taxonomy",
                    "ParentElementCode: TXA",
                    "ElementType: Table",
                    "ElementCode: TBA",
                    "ElementLabelFi: TBA table (label fi)",
                    "Change: DELETED",
                    "Notes: "
                )
            }
        }
    }

    @Nested
    inner class IdentificationNote {

        @Test
        fun `Should provide identifying Note when reported ReportingFramework is not having Label translation for any IdentificationLabelLanguage`() {
            executeDpmCompareForSectionAndExpectSuccess(
                identificationLabelLanguages = "fi,sv",
                baselineSql = "DELETE from 'mReportingFramework' WHERE FrameworkID = 501",
                currentSql = "DELETE from 'mConceptTranslation' WHERE ConceptID = 501",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFB",
                    "ElementLabelFi: ",
                    "ElementLabelSv: ",
                    "Change: ADDED",
                    "Notes: CURRENT ROW: #- Element Id: 501 #- Element Label: RFB framework"
                )
            }
        }

        @Test
        fun `Should not provide identifying Note when reported ReportingFramework is having Label translation at least for one IdentificationLabelLanguage`() {
            executeDpmCompareForSectionAndExpectSuccess(
                identificationLabelLanguages = "fi,sv",
                baselineSql = "DELETE from 'mReportingFramework' WHERE FrameworkID = 501",
                currentSql = "DELETE from 'mConceptTranslation' WHERE ConceptID = 501 AND LanguageID != 2",
                expectedChanges = 1
            ) { _, outputFileContent ->
                assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                    "ParentElementType: ",
                    "ParentElementCode: ",
                    "ElementType: ReportingFramework",
                    "ElementCode: RFB",
                    "ElementLabelFi: ",
                    "ElementLabelSv: RFB framework (label sv)",
                    "Change: ADDED",
                    "Notes: "
                )
            }
        }
    }

    @Test
    fun `Should report ADDED & DELETED when FrameworkCode is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mReportingFramework' SET FrameworkCode = NULL WHERE FrameworkID = 501",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Rep_Fw_Overview")).containsExactly(
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: ReportingFramework",
                "ElementCode: ",
                "ElementLabelFi: RFB framework (label fi)",
                "Change: ADDED",
                "Notes: CURRENT ROW: #- Element Id: 501 #- Element Label: RFB framework",
                "-----------",
                "ParentElementType: ",
                "ParentElementCode: ",
                "ElementType: ReportingFramework",
                "ElementCode: RFB",
                "ElementLabelFi: RFB framework (label fi)",
                "Change: DELETED",
                "Notes: "
            )
        }
    }
}
