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

class HierarchyNodeStructureSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val hierarchyId = FallbackField(
        fieldName = "HierarchyId"
    )

    private val hierarchyInherentLabel = FallbackField(
        fieldName = "HierarchyLabel"
    )

    private val memberId = FallbackField(
        fieldName = "MemberId"
    )

    private val memberInherentLabel = FallbackField(
        fieldName = "MemberLabel"
    )

    private val hierarchyNodeInherentLabel = FallbackField(
        fieldName = "HierarchyNodeLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(hierarchyId, memberId, hierarchyNodeInherentLabel)
    )

    private val hierarchyCode = KeySegmentField(
        fieldName = "HierarchyCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = hierarchyInherentLabel
    )

    private val memberCode = KeySegmentField(
        fieldName = "MemberCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = memberInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "HierarchyNodeLabel"
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
        sectionDescription = "Added and deleted HierarchyNodes, changes in Parent Member, Order and Level details",
        sectionFields = listOf(
            hierarchyId,
            hierarchyInherentLabel,
            memberId,
            memberInherentLabel,
            hierarchyNodeInherentLabel,
            recordIdentityFallback,
            hierarchyCode,
            memberCode,
            *identificationLabels,
            changeKind,
            parentMemberCode,
            order,
            level,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(hierarchyCode),
            NumberAwareSort(memberCode),
            FixedChangeKindSort(changeKind)
        ),

        //Report all changes (i.e. also adds/deletes) as those explain why
        //subsequent nodes order/level values change
        includedChanges = ChangeKind.allChanges()
    )

    override val queryColumnMapping = mapOf(
        "HierarchyId" to hierarchyId,
        "HierarchyInherentLabel" to hierarchyInherentLabel,
        "MemberId" to memberId,
        "MemberInherentLabel" to memberInherentLabel,
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
        ,mHierarchy.HierarchyLabel AS 'HierarchyInherentLabel'
        ,mHierarchyNode.MemberID AS 'MemberId'
        ,mMember.MemberLabel AS 'MemberInherentLabel'
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
