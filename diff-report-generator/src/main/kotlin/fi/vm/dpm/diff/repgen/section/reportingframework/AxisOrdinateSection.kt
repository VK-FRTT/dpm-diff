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

class AxisOrdinateSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {

    // Scope
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

    private val axisInherentLabel = FallbackField(
        fieldName = "AxisLabel"
    )

    private val axisOrientation = KeySegmentField(
        fieldName = "AxisOrientation",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = axisInherentLabel
    )

    // AxisOrdinate
    private val ordinateId = FallbackField(
        fieldName = "OrdinateId"
    )

    private val ordinateInherentLabel = FallbackField(
        fieldName = "OrdinateLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(ordinateId, ordinateInherentLabel)
    )

    private val ordinateCode = KeySegmentField(
        fieldName = "OrdinateCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = ordinateInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "OrdinateLabel"
    )

    private val level = AtomField(
        fieldName = "Level"
    )

    private val order = AtomField(
        fieldName = "Order"
    )

    private val parentOrdinateCode = AtomField(
        fieldName = "ParentOrdinateCode"
    )

    // Atoms
    private val isDisplayBeforeChildren = AtomField(
        fieldName = "IsDisplayBeforeChildren"
    )
    private val isAbstractHeader = AtomField(
        fieldName = "IsAbstractHeader"
    )
    private val isRowKey = AtomField(
        fieldName = "IsRowKey"
    )

    private val typeOfKey = AtomField(
        fieldName = "TypeOfKey"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "AxisOrdinates",
        sectionTitle = "Axis Ordinates",
        sectionDescription = "Axis Ordinates: changes in Axis Ordinates",
        sectionFields = listOf(
            taxonomyInherentLabel,
            taxonomyCode,
            tableInherentLabel,
            tableCode,
            axisInherentLabel,
            axisOrientation,
            ordinateId,
            ordinateInherentLabel,
            recordIdentityFallback,
            ordinateCode,
            *identificationLabels,
            changeKind,
            level,
            order,
            parentOrdinateCode,
            isDisplayBeforeChildren,
            isAbstractHeader,
            isRowKey,
            typeOfKey,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(taxonomyCode),
            NumberAwareSort(tableCode),
            NumberAwareSort(axisOrientation),
            NumberAwareSort(ordinateCode),
            FixedChangeKindSort(changeKind)
        ),
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "TaxonomyInherentLabel" to taxonomyInherentLabel,
        "TaxonomyCode" to taxonomyCode,
        "TableInherentLabel" to tableInherentLabel,
        "TableCode" to tableCode,
        "AxisInherentLabel" to axisInherentLabel,
        "AxisOrientation" to axisOrientation,
        "OrdinateId" to ordinateId,
        "OrdinateInherentLabel" to ordinateInherentLabel,
        "OrdinateCode" to ordinateCode,
        *idLabelColumnMapping(),
        "Level" to level,
        "Order" to order,
        "ParentOrdinateCode" to parentOrdinateCode,
        "IsDisplayBeforeChildren" to isDisplayBeforeChildren,
        "IsAbstractHeader" to isAbstractHeader,
        "IsRowKey" to isRowKey,
        "TypeOfKey" to typeOfKey
    )

    override val query = """
        SELECT
        mTaxonomy.TaxonomyLabel AS 'TaxonomyInherentLabel'
        ,mTaxonomy.TaxonomyCode AS 'TaxonomyCode'
        ,mTable.TableLabel AS 'TableInherentLabel'
        ,mTable.TableCode AS 'TableCode'
        ,mAxis.AxisLabel AS 'AxisInherentLabel'
        ,mAxis.AxisOrientation AS 'AxisOrientation'
        ,mAxisOrdinate.OrdinateID AS 'OrdinateId'
        ,mAxisOrdinate.OrdinateLabel AS 'OrdinateInherentLabel'
        ,mAxisOrdinate.OrdinateCode AS 'OrdinateCode'
        ${idLabelAggregateFragment()}
        ,mAxisOrdinate.Level AS 'Level'
        ,mAxisOrdinate.'Order' AS 'Order'
        ,ParentAxisOrdinate.OrdinateCode AS 'ParentOrdinateCode'
        ,mAxisOrdinate.IsDisplayBeforeChildren AS 'IsDisplayBeforeChildren'
        ,mAxisOrdinate.IsAbstractHeader AS 'IsAbstractHeader'
        ,mAxisOrdinate.IsRowKey AS 'IsRowKey'
        ,mAxisOrdinate.TypeOfKey AS 'TypeOfKey'

		FROM mAxisOrdinate
        LEFT JOIN mAxis ON mAxis.AxisID = mAxisOrdinate.AxisID
        LEFT JOIN mTableAxis ON mTableAxis.AxisID = mAxisOrdinate.AxisID
        LEFT JOIN mTable ON mTable.TableID = mTableAxis.TableID
        LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
        LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mAxisOrdinate.ConceptID
        LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID
        LEFT JOIN mAxisOrdinate AS ParentAxisOrdinate ON ParentAxisOrdinate.OrdinateID = mAxisOrdinate.ParentOrdinateID

        WHERE
        (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

        GROUP BY mAxisOrdinate.OrdinateID

        ORDER BY mTaxonomy.TaxonomyCode ASC, mTable.TableCode ASC, mAxis.AxisOrientation ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        "mAxisOrdinate"
    )

    init {
        sanityCheckSectionConfig()
    }
}
