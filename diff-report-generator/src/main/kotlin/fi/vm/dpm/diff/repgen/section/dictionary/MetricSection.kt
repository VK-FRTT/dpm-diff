package fi.vm.dpm.diff.repgen.section.dictionary

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

class MetricSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val domainInherentLabel = FallbackField(
        fieldName = "DomainLabel"
    )

    private val domainCode = KeySegmentField(
        fieldName = "DomainCode",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = domainInherentLabel
    )

    private val metricId = FallbackField(
        fieldName = "MetricId"
    )

    private val metricInherentLabel = FallbackField(
        fieldName = "MetricLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(metricId, metricInherentLabel)
    )

    private val metricCode = KeySegmentField(
        fieldName = "MetricCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = metricInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "MetricLabel"
    )

    private val dataType = AtomField(
        fieldName = "DataType"
    )

    private val flowType = AtomField(
        fieldName = "FlowType"
    )

    private val balanceType = AtomField(
        fieldName = "BalanceType"
    )

    private val referencedDomainCode = AtomField(
        fieldName = "ReferencedDomain"
    )

    private val referencedHierarchyCode = AtomField(
        fieldName = "ReferencedHierarchy"
    )

    private val hierarchyStartingMemberCode = AtomField(
        fieldName = "HierarchyStartingMember"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Metric",
        sectionTitle = "Metrics",
        sectionDescription = "Added and deleted Metrics, changes in DataType, FlowType, BalanceType, Domain reference and Hierarchy reference",
        sectionFields = listOf(
            domainInherentLabel,
            domainCode,
            metricId,
            metricInherentLabel,
            recordIdentityFallback,
            metricCode,
            *identificationLabels,
            changeKind,
            dataType,
            flowType,
            balanceType,
            referencedDomainCode,
            referencedHierarchyCode,
            hierarchyStartingMemberCode,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(domainCode),
            NumberAwareSort(metricCode),
            FixedChangeKindSort(changeKind)
        ),
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "DomainInherentLabel" to domainInherentLabel,
        "DomainCode" to domainCode,
        "MetricId" to metricId,
        "MetricInherentLabel" to metricInherentLabel,
        "MetricCode" to metricCode,
        *idLabelColumnMapping(),
        "DataType" to dataType,
        "FlowType" to flowType,
        "BalanceType" to balanceType,
        "ReferencedDomainCode" to referencedDomainCode,
        "ReferencedHierarchyCode" to referencedHierarchyCode,
        "HierarchyStartingMemberCode" to hierarchyStartingMemberCode
    )

    override val query = """
        SELECT
        mDomain.DomainLabel AS 'DomainInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
        ,mMetric.MetricID AS 'MetricId'
        ,mMember.MemberLabel AS 'MetricInherentLabel'
        ,mMember.MemberCode AS 'MetricCode'
        ${idLabelAggregateFragment()}
        ,mMetric.DataType AS 'DataType'
        ,mMetric.FlowType AS 'FlowType'
        ,mMetric.BalanceType AS 'BalanceType'
        ,ReferencedDomain.DomainCode AS 'ReferencedDomainCode'
        ,ReferencedHierarchy.HierarchyCode AS 'ReferencedHierarchyCode'
        ,HierarchyStartingMember.MemberCode AS 'HierarchyStartingMemberCode'

        FROM mMetric
        LEFT JOIN mMember ON mMember.MemberID = mMetric.CorrespondingMemberID
        LEFT JOIN mDomain ON mDomain.DomainID = mMember.DomainID
        LEFT JOIN mDomain AS ReferencedDomain ON ReferencedDomain.DomainID = mMetric.ReferencedDomainID
        LEFT JOIN mHierarchy AS ReferencedHierarchy ON ReferencedHierarchy.HierarchyID = mMetric.ReferencedHierarchyID
        LEFT JOIN mMember AS HierarchyStartingMember ON HierarchyStartingMember.MemberID = mMetric.HierarchyStartingMemberID
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mMember.ConceptID
        LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID

        WHERE
        mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

        GROUP BY mMetric.MetricID

        ORDER BY mDomain.DomainCode ASC, mMember.MemberCode ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        "mMetric"
    )

    init {
        sanityCheckSectionConfig()
    }
}
