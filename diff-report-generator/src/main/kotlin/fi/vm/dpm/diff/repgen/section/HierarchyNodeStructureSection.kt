package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.CorrelationKeyField
import fi.vm.dpm.diff.model.CorrelationKeyKind
import fi.vm.dpm.diff.model.CorrelationMode
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class HierarchyNodeStructureSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val hierarchyId = FallbackField(
        fieldName = "HierarchyId"
    )

    private val memberId = FallbackField(
        fieldName = "MemberId"
    )

    private val hierarchyNodeInherentLabel = FallbackField(
        fieldName = "HierarchyNodeLabel"
    )

    private val hierarchyCode = CorrelationKeyField(
        fieldName = "HierarchyCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = hierarchyNodeInherentLabel,
        noteFallbacks = listOf(hierarchyId, memberId, hierarchyNodeInherentLabel)
    )

    private val memberCode = CorrelationKeyField(
        fieldName = "MemberCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = hierarchyNodeInherentLabel,
        noteFallbacks = listOf(hierarchyId, memberId, hierarchyNodeInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "HierarchyNodeLabel",
        fallbackField = hierarchyNodeInherentLabel
    )

    private val parentMemberCode = AtomField(
        fieldName = "ParentMemberCode"
    )

    private val order = AtomField(
        fieldName = "Order"
    )

    private val level = AtomField(
        fieldName = "Level"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "HierNodeStructure",
        sectionTitle = "HierarchyNodes structure",
        sectionDescription = "HierarchyNodes: Parent Member, Order and Level changes",
        sectionFields = listOf(
            hierarchyId,
            memberId,
            hierarchyNodeInherentLabel,
            hierarchyCode,
            memberCode,
            *identificationLabels,
            changeKind,
            parentMemberCode,
            order,
            level,
            note
        ),
        correlationMode = CorrelationMode.ONE_PHASE_BY_FULL_KEY,
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "HierarchyId" to hierarchyId,
        "MemberId" to memberId,
        "HierarchyNodeInherentLabel" to hierarchyNodeInherentLabel,
        "HierarchyCode" to hierarchyCode,
        "MemberCode" to memberCode,
        *idLabelColumnMapping(),
        "ParentMemberCode" to parentMemberCode,
        "Order" to order,
        "Level" to level
    )

    override val query = """
        SELECT
        mHierarchyNode.HierarchyID AS 'HierarchyId'
        ,mHierarchyNode.MemberID AS 'MemberId'
        ,mHierarchyNode.HierarchyNodeLabel AS 'HierarchyNodeInherentLabel'
        ,mHierarchy.HierarchyCode AS 'HierarchyCode'
        ,mMember.MemberCode AS 'MemberCode'
        ${idLabelAggregateFragment()}
        ,ParentMember.MemberCode AS 'ParentMemberCode'
        ,mHierarchyNode.'Order' AS 'Order'
        ,mHierarchyNode.Level AS 'Level'

        FROM mHierarchyNode
        LEFT JOIN mHierarchy ON mHierarchy.HierarchyID = mHierarchyNode.HierarchyID
        LEFT JOIN mMember ON mMember.MemberID = mHierarchyNode.MemberID
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mHierarchyNode.ConceptID
        LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID
        LEFT JOIN mMember AS ParentMember ON ParentMember.MemberID = mHierarchyNode.ParentMemberID

        WHERE
        (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

        GROUP BY mHierarchyNode.HierarchyID, mHierarchyNode.MemberID

        ORDER BY mHierarchy.HierarchyCode ASC, mHierarchyNode.'Order' ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        "mHierarchyNode"
    )

    init {
        sanityCheckSectionConfig()
    }
}
