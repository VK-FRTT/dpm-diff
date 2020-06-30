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

class HierarchyNodeSection(
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

    private val isAbstract = AtomField(
        fieldName = "IsAbstract"
    )

    private val comparisonOperator = AtomField(
        fieldName = "ComparisonOperator"
    )

    private val unaryOperator = AtomField(
        fieldName = "UnaryOperator"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "HierNode",
        sectionTitle = "HierarchyNodes",
        sectionDescription = "HierarchyNodes: ComparisonOperator, UnaryOperator and IsAbstract changes",
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
            isAbstract,
            comparisonOperator,
            unaryOperator,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(hierarchyCode),
            NumberAwareSort(memberCode),
            FixedChangeKindSort(changeKind)
        ),
        includedChanges = ChangeKind.allValues()
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
        "IsAbstract" to isAbstract,
        "ComparisonOperator" to comparisonOperator,
        "UnaryOperator" to unaryOperator
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
        ,mHierarchyNode.IsAbstract AS 'IsAbstract'
        ,mHierarchyNode.ComparisonOperator AS 'ComparisonOperator'
        ,mHierarchyNode.UnaryOperator AS 'UnaryOperator'

        FROM mHierarchyNode
        LEFT JOIN mHierarchy ON mHierarchy.HierarchyID = mHierarchyNode.HierarchyID
        LEFT JOIN mMember ON mMember.MemberID = mHierarchyNode.MemberID
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mHierarchyNode.ConceptID
        LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID

        WHERE
        (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

        GROUP BY mHierarchyNode.HierarchyID, mHierarchyNode.MemberID

        ORDER BY mHierarchy.HierarchyCode ASC, mMember.MemberCode ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        "mHierarchyNode"
    )

    init {
        sanityCheckSectionConfig()
    }
}
