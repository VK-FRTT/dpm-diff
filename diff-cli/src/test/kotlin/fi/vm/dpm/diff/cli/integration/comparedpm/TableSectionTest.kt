package fi.vm.dpm.diff.cli.integration.comparedpm

import fi.vm.dpm.diff.cli.integration.DpmDiffCliCompareTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--compareDpm´ Table")
internal class TableSectionTest : DpmDiffCliCompareTestBase(
    section = "Table",
    commonSetupSql = compareDpmSetupSql()
) {

    @Test
    fun `Should report ADDED when Current has a new Table`() {
        executeDpmCompareForSectionAndExpectSuccess(
            baselineSql = "DELETE from 'mTable' WHERE TableID = 800",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "TableLabelFi: TBA table (label fi)",
                "Change: ADDED",
                "FilingIndicator: ",
                "FilingIndicator(baseline): ",
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
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "TableLabelFi: TBA table (label fi)",
                "Change: DELETED",
                "FilingIndicator: ",
                "FilingIndicator(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report ADDED & DELETED when TableCode is set NULL in Current`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mTable' SET TableCode = NULL WHERE TableID = 800",
            expectedChanges = 2
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: ",
                "TableLabelFi: TBA table (label fi)",
                "Change: ADDED",
                "FilingIndicator: ",
                "FilingIndicator(baseline): ",
                "Notes: CURRENT ROW: #- Table Id: 800 #- Table Label: TBA table",
                "-----------",
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "TableLabelFi: TBA table (label fi)",
                "Change: DELETED",
                "FilingIndicator: ",
                "FilingIndicator(baseline): ",
                "Notes: "
            )
        }
    }

    @Test
    fun `Should report MODIFIED when XbrlFilingIndicatorCode value is changed`() {
        executeDpmCompareForSectionAndExpectSuccess(
            currentSql = "UPDATE 'mTable' SET XbrlFilingIndicatorCode = NULL WHERE TableID = 800",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "TableLabelFi: TBA table (label fi)",
                "Change: MODIFIED",
                "FilingIndicator: ",
                "FilingIndicator(baseline): filing-indicator",
                "Notes: MODIFIED: #- Filing Indicator"
            )
        }
    }

    @Test
    fun `Should provide identifying Note when reported Table is not having Label translation for any IdentificationLabelLanguage`() {
        executeDpmCompareForSectionAndExpectSuccess(
            identificationLabelLanguages = "fi,sv",
            baselineSql = "DELETE from 'mTable' WHERE TableID = 800",
            currentSql = "DELETE from 'mConceptTranslation' WHERE ConceptID = 800",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "TableLabelFi: ",
                "TableLabelSv: ",
                "Change: ADDED",
                "FilingIndicator: ",
                "FilingIndicator(baseline): ",
                "Notes: CURRENT ROW: #- Table Id: 800 #- Table Label: TBA table"
            )
        }
    }

    @Test
    fun `Should not provide identifying Note when reported Table is having Label translation at least for one IdentificationLabelLanguage`() {
        executeDpmCompareForSectionAndExpectSuccess(
            identificationLabelLanguages = "fi,sv",
            baselineSql = "DELETE from 'mTable' WHERE TableID = 800",
            currentSql = "DELETE from 'mConceptTranslation' WHERE ConceptID = 800 AND LanguageID != 2",
            expectedChanges = 1
        ) { _, outputFileContent ->
            assertThat(outputFileContent.transposeSectionSheetAsList("01_Table")).containsExactly(
                "TaxonomyCode: TXA",
                "TableCode: TBA",
                "TableLabelFi: ",
                "TableLabelSv: TBA table (label sv)",
                "Change: ADDED",
                "FilingIndicator: ",
                "FilingIndicator(baseline): ",
                "Notes: "
            )
        }
    }
}
