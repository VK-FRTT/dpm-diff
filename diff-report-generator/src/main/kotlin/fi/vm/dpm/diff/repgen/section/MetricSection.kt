package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.DifferenceKind
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class MetricSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    override val includedDifferenceKinds: Array<DifferenceKind> = DifferenceKind.values()

    private val metricId = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "MetricId"
    )

    private val metricInherentLabel = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "MetricLabel"
    )

    private val domainCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "DomainCode",
        correlationKeyFallback = metricInherentLabel,
        noteFallback = listOf(metricId, metricInherentLabel)
    )

    private val metricCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "MetricCode",
        correlationKeyFallback = metricInherentLabel,
        noteFallback = listOf(metricId, metricInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "MetricLabel",
        noteFallback = metricInherentLabel
    )

    private val dataType = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "DataType"
    )

    private val flowType = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "FlowType"
    )

    private val balanceType = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "BalanceType"
    )

    private val referencedDomainCode = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "ReferencedDomain"
    )

    private val referencedHierarchyCode = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "ReferencedHierarchy"
    )

    private val hierarchyStartingMemberCode = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "HierarchyStartingMember"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Metric",
        sectionTitle = "Metrics",
        sectionDescription = "Metrics: DataType, FlowType, BalanceType, Domain reference and Hierarchy reference changes",
        sectionFields = listOf(
            metricId,
            metricInherentLabel,
            domainCode,
            metricCode,
            *identificationLabels,
            differenceKind,
            dataType,
            flowType,
            balanceType,
            referencedDomainCode,
            referencedHierarchyCode,
            hierarchyStartingMemberCode,
            note
        )
    )

    override val queryColumnMapping = mapOf(
        "MetricID" to metricId,
        "MetricInherentLabel" to metricInherentLabel,
        "DomainCode" to domainCode,
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
        mMetric.MetricID AS 'MetricID'
        ,mMember.MemberLabel AS 'MetricInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
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
