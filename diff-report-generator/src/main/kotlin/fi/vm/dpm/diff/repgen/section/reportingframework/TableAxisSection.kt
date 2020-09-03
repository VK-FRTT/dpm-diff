package fi.vm.dpm.diff.repgen.section.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.KeySegmentField
import fi.vm.dpm.diff.model.KeySegmentKind
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.SectionBase

class TableAxisSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val taxonomyInherentLabel = FallbackField(
        fieldName = "TaxonomyLabel"
    )

    private val taxonomyCode = KeySegmentField(
        fieldName = "TaxonomyCode",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = taxonomyInherentLabel
    )

    private val tableInherentLabel = FallbackField(
        fieldName = "TableLabel"
    )

    private val tableCode = KeySegmentField(
        fieldName = "TableCode",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = tableInherentLabel
    )

    private val axisId = FallbackField(
        fieldName = "AxisId"
    )

    private val axisInherentLabel = FallbackField(
        fieldName = "AxisLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(axisId, axisInherentLabel)
    )

    private val axisOrientation = KeySegmentField(
        fieldName = "AxisOrientation",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = axisInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "AxisLabel"
    )

    private val order = AtomField(
        fieldName = "Order"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "TableAxis",
        sectionTitle = "TableAxis",
        sectionDescription = "Added and deleted Table Axis, changes in Order",
        sectionFields = listOf(
            taxonomyInherentLabel,
            taxonomyCode,
            tableInherentLabel,
            tableCode,
            axisId,
            axisInherentLabel,
            recordIdentityFallback,
            axisOrientation,
            *identificationLabels,
            changeKind,
            order,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(taxonomyCode),
            NumberAwareSort(tableCode),
            NumberAwareSort(axisOrientation),
            FixedChangeKindSort(changeKind)
        ),
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "TaxonomyInherentLabel" to taxonomyInherentLabel,
        "TaxonomyCode" to taxonomyCode,
        "TableInherentLabel" to tableInherentLabel,
        "TableCode" to tableCode,
        "AxisId" to axisId,
        "AxisInherentLabel" to axisInherentLabel,
        "AxisOrientation" to axisOrientation,
        *idLabelColumnMapping(),
        "Order" to order
    )

    override val query = """
        SELECT
        mTaxonomy.TaxonomyLabel AS 'TaxonomyInherentLabel'
        ,mTaxonomy.TaxonomyCode AS 'TaxonomyCode'
        ,mTable.TableLabel AS 'TableInherentLabel'
        ,mTable.TableCode AS 'TableCode'
        ,mAxis.AxisID AS 'AxisId'
        ,mAxis.AxisLabel AS 'AxisInherentLabel'
        ,mAxis.AxisOrientation AS 'AxisOrientation'
        ${idLabelAggregateFragment()}
        ,'mAxisTable.Order' AS 'Order'

        FROM mTableAxis
        LEFT JOIN mAxis ON mAxis.AxisID = mTableAxis.AxisID
        LEFT JOIN mTable ON mTable.TableID = mTableAxis.TableID
        LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
        LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mAxis.ConceptID
        LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID

        WHERE
        (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

        GROUP BY mAxis.AxisID

        ORDER BY mTaxonomy.TaxonomyCode ASC, mTable.TableCode ASC, mAxis.AxisOrientation ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        "mTableAxis"
    )

    init {
        sanityCheckSectionConfig()
    }
}
