package fi.vm.dpm.diff.repgen.section.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyKind
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.SectionBase

class TableSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val taxonomyInherentLabel = FallbackField(
        fieldName = "TaxonomyLabel"
    )

    private val taxonomyCode = KeyField(
        fieldName = "TaxonomyCode",
        keyKind = KeyKind.PRIMARY_SCOPE_KEY,
        keyFallback = taxonomyInherentLabel
    )

    private val tableId = FallbackField(
        fieldName = "TableId"
    )

    private val tableInherentLabel = FallbackField(
        fieldName = "TableLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(tableId, tableInherentLabel)
    )

    private val tableCode = KeyField(
        fieldName = "TableCode",
        keyKind = KeyKind.PRIMARY_SCOPE_KEY,
        keyFallback = tableInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "TableLabel"
    )

    private val xbrlFilingIndicator = AtomField(
        fieldName = "FilingIndicator"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Table",
        sectionTitle = "Table",
        sectionDescription = "Tables: Filing indicator changes",
        sectionFields = listOf(
            taxonomyInherentLabel,
            taxonomyCode,
            tableId,
            tableInherentLabel,
            recordIdentityFallback,
            tableCode,
            *identificationLabels,
            changeKind,
            xbrlFilingIndicator,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(taxonomyCode),
            NumberAwareSort(tableCode),
            FixedChangeKindSort(changeKind)
        ),
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "TaxonomyInherentLabel" to taxonomyInherentLabel,
        "TaxonomyCode" to taxonomyCode,
        "TableId" to tableId,
        "TableInherentLabel" to tableInherentLabel,
        "TableCode" to tableCode,
        *idLabelColumnMapping(),
        "XbrlFilingIndicator" to xbrlFilingIndicator
    )

    override val query = """
        SELECT
        mTaxonomy.TaxonomyLabel AS 'TaxonomyInherentLabel'
        ,mTaxonomy.TaxonomyCode AS 'TaxonomyCode'
        ,mTable.TableID AS 'TableId'
        ,mTable.TableLabel AS 'TableInherentLabel'
        ,mTable.TableCode AS 'TableCode'
        ${idLabelAggregateFragment()}
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

    override val sourceTableDescriptors = listOf(
        "mTable"
    )

    init {
        sanityCheckSectionConfig()
    }
}
