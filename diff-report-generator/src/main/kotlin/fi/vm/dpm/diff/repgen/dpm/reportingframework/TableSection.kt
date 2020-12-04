package fi.vm.dpm.diff.repgen.dpm.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.KeySegmentField
import fi.vm.dpm.diff.model.KeySegmentKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.dpm.utils.DpmSectionIdentificationLabels

object TableSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val taxonomyInherentLabel = FallbackField(
            fieldName = "TaxonomyLabel"
        )

        val taxonomyCode = KeySegmentField(
            fieldName = "TaxonomyCode",
            segmentKind = KeySegmentKind.SCOPE_SEGMENT,
            segmentFallback = taxonomyInherentLabel
        )

        val tableId = FallbackField(
            fieldName = "TableId"
        )

        val tableInherentLabel = FallbackField(
            fieldName = "TableLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(tableId, tableInherentLabel)
        )

        val tableCode = KeySegmentField(
            fieldName = "TableCode",
            segmentKind = KeySegmentKind.SCOPE_SEGMENT,
            segmentFallback = tableInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "TableLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        val xbrlFilingIndicator = AtomField(
            fieldName = "FilingIndicator"
        )

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "Table",
            sectionTitle = "Tables",
            sectionDescription = "Added and deleted Tables, changes in FilingIndicator",
            sectionFields = listOf(
                taxonomyInherentLabel,
                taxonomyCode,
                tableId,
                tableInherentLabel,
                recordIdentityFallback,
                tableCode,
                *identificationLabels.labelFields(),
                changeKind,
                xbrlFilingIndicator,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSort(taxonomyCode),
                NumberAwareSort(tableCode),
                FixedChangeKindSort(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "TaxonomyInherentLabel" to taxonomyInherentLabel,
            "TaxonomyCode" to taxonomyCode,
            "TableId" to tableId,
            "TableInherentLabel" to tableInherentLabel,
            "TableCode" to tableCode,
            *identificationLabels.labelColumnMapping(),
            "XbrlFilingIndicator" to xbrlFilingIndicator
        )

        val query = """
            SELECT
            mTaxonomy.TaxonomyLabel AS 'TaxonomyInherentLabel'
            ,mTaxonomy.TaxonomyCode AS 'TaxonomyCode'
            ,mTable.TableID AS 'TableId'
            ,mTable.TableLabel AS 'TableInherentLabel'
            ,mTable.TableCode AS 'TableCode'
             ${identificationLabels.labelAggregateFragment()}
            ,mTable.XbrlFilingIndicatorCode AS 'XbrlFilingIndicator'

            FROM mTable
            LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
            LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mTable.ConceptID
            LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID

            WHERE
            (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

            GROUP BY mTable.TableID

            ORDER BY mTaxonomy.TaxonomyCode ASC, mTable.TableCode ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mTable"
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
