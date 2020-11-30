package fi.vm.dpm.diff.repgen.dpm.section.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.DisplayHint
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.KeySegmentField
import fi.vm.dpm.diff.model.KeySegmentKind
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.dpm.DpmGenerationContext
import fi.vm.dpm.diff.repgen.dpm.section.SectionBase

class OrdinateCategorisationSection(
    generationContext: DpmGenerationContext
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

    // OrdinateCategorisation
    private val ordinateId = FallbackField(
        fieldName = "OrdinateId"
    )

    private val ordinateInherentLabel = FallbackField(
        fieldName = "OrdinateLabel"
    )

    private val ordinateCode = KeySegmentField(
        fieldName = "OrdinateCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = ordinateInherentLabel
    )

    private val dimensionId = FallbackField(
        fieldName = "DimensionId"
    )

    private val dimensionInherentLabel = FallbackField(
        fieldName = "DimensionLabel"
    )

    private val dimensionCode = KeySegmentField(
        fieldName = "DimensionCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = dimensionInherentLabel
    )

    private val memberId = FallbackField(
        fieldName = "MemberId"
    )

    private val memberInherentLabel = FallbackField(
        fieldName = "MemberLabel"
    )

    private val memberCode = KeySegmentField(
        fieldName = "MemberCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = memberInherentLabel
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(ordinateId, dimensionId, memberId)
    )

    // Atoms
    private val source = AtomField(
        fieldName = "Source"
    )

    private val dps = AtomField(
        fieldName = "Dps",
        displayHint = DisplayHint.FIXED_EXTRA_WIDE
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "OrdCat",
        sectionTitle = "OrdinateCategorisations",
        sectionDescription = "Added and deleted OrdinateCategorisations",
        sectionFields = listOf(
            taxonomyInherentLabel,
            taxonomyCode,
            tableInherentLabel,
            tableCode,
            axisInherentLabel,
            axisOrientation,
            ordinateId,
            ordinateInherentLabel,
            ordinateCode,
            dimensionId,
            dimensionInherentLabel,
            dimensionCode,
            memberId,
            memberInherentLabel,
            memberCode,
            recordIdentityFallback,
            changeKind,
            source,
            dps,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(taxonomyCode),
            NumberAwareSort(tableCode),
            NumberAwareSort(axisOrientation),
            NumberAwareSort(ordinateCode),
            NumberAwareSort(dimensionCode),
            NumberAwareSort(memberCode),
            FixedChangeKindSort(changeKind)
        ),
        includedChanges = ChangeKind.allChanges()
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

        "DimensionId" to dimensionId,
        "DimensionInherentLabel" to dimensionInherentLabel,
        "DimensionCode" to dimensionCode,

        "MemberId" to memberId,
        "MemberInherentLabel" to memberInherentLabel,
        "MemberCode" to memberCode,

        "Source" to source,
        "DPS" to dps
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
        ,mDimension.DimensionID AS 'DimensionId'
        ,mDimension.DimensionLabel AS 'DimensionInherentLabel'
        ,mDimension.DimensionCode AS 'DimensionCode'
        ,mMember.MemberID AS 'MemberId'
        ,mMember.MemberLabel AS 'MemberInherentLabel'
        ,mMember.MemberCode AS 'MemberCode'
        ,mOrdinateCategorisation.Source AS 'Source'
        ,mOrdinateCategorisation.DPS AS 'DPS'

		FROM mOrdinateCategorisation
		LEFT JOIN mAxisOrdinate ON mAxisOrdinate.OrdinateID = mOrdinateCategorisation.OrdinateID
        LEFT JOIN mAxis ON mAxis.AxisID = mAxisOrdinate.AxisID
        LEFT JOIN mTableAxis ON mTableAxis.AxisID = mAxisOrdinate.AxisID
        LEFT JOIN mTable ON mTable.TableID = mTableAxis.TableID
        LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
        LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
		LEFT JOIN mDimension ON mDimension.DimensionID = mOrdinateCategorisation.DimensionID
		LEFT JOIN mMember ON mMember.MemberID = mOrdinateCategorisation.MemberID

        ORDER BY mTaxonomy.TaxonomyCode ASC, mTable.TableCode ASC, mAxis.AxisOrientation ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        "mOrdinateCategorisation"
    )

    init {
        sanityCheckSectionConfig()
    }
}
