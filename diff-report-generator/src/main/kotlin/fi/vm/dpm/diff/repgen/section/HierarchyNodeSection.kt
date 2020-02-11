package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.DifferenceKind
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class HierarchyNodeSection(
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

    override val identificationLabels = composeIdentificationLabelFields(
        noteFallback = hierarchyNodeInherentLabel
    ) {
        "HierarchyNodeLabel$it"
    }

    private val isAbstract = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "IsAbstract"
    )

    private val comparisonOperator = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "ComparisonOperator"
    )

    private val unaryOperator = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "UnaryOperator"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "HierNode",
        sectionTitle = "HierarchyNodes",
        sectionDescription = "HierarchyNodes: ComparisonOperator, UnaryOperator and IsAbstract changes",
        sectionFields = listOf(
            hierarchyId,
            memberId,
            hierarchyNodeInherentLabel,
            hierarchyCode,
            memberCode,
            *identificationLabels,
            differenceKind,
            isAbstract,
            comparisonOperator,
            unaryOperator,
            note
        )
    )

    override val queryColumnMapping = mapOf(
        "HierarchyId" to hierarchyId,
        "MemberId" to memberId,
        "HierarchyNodeInherentLabel" to hierarchyNodeInherentLabel,
        "HierarchyCode" to hierarchyCode,
        "MemberCode" to memberCode,
        *composeIdentificationLabelColumnNames(),
        "IsAbstract" to isAbstract,
        "ComparisonOperator" to comparisonOperator,
        "UnaryOperator" to unaryOperator
    )

    override val query = """
        SELECT
        mHierarchyNode.HierarchyID AS 'HierarchyId'
        ,mHierarchyNode.MemberID AS 'MemberId'
        ,mHierarchyNode.HierarchyNodeLabel AS 'HierarchyNodeInherentLabel'
        ,mHierarchy.HierarchyCode AS 'HierarchyCode'
        ,mMember.MemberCode AS 'MemberCode'
        ${composeIdentificationLabelQueryFragment("mLanguage.IsoCode", "mConceptTranslation.Text")}
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

    override val primaryTables = listOf(
        "mHierarchyNode"
    )

    init {
        sanityCheckSectionConfig()
    }
}
