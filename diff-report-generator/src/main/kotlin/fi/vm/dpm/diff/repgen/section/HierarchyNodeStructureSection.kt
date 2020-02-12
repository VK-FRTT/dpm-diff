package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.DifferenceKind
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class HierarchyNodeStructureSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    override val includedDifferenceKinds: Array<DifferenceKind> = DifferenceKind.values()

    private val hierarchyId = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "HierarchyId"
    )

    private val memberId = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "MemberId"
    )

    private val hierarchyNodeInherentLabel = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "HierarchyNodeLabel"
    )

    private val hierarchyCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "HierarchyCode",
        correlationKeyFallback = hierarchyNodeInherentLabel,
        noteFallback = listOf(hierarchyId, memberId, hierarchyNodeInherentLabel)
    )

    private val memberCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "MemberCode",
        correlationKeyFallback = hierarchyNodeInherentLabel,
        noteFallback = listOf(hierarchyId, memberId, hierarchyNodeInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "HierarchyNodeLabel",
        noteFallback = hierarchyNodeInherentLabel
    )

    private val parentMemberCode = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "ParentMemberCode"
    )

    private val order = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "Order"
    )

    private val level = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
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
            differenceKind,
            parentMemberCode,
            order,
            level,
            note
        )
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
