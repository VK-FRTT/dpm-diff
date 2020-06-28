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
import fi.vm.dpm.diff.repgen.SourceTableDescriptor
import fi.vm.dpm.diff.repgen.section.SectionBase

class MemberSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val domainInherentLabel = FallbackField(
        fieldName = "DomainLabel"
    )

    private val domainCode = KeySegmentField(
        fieldName = "DomainCode",
        segmentKind = KeySegmentKind.SCOPING_TOP_LEVEL_SEGMENT,
        segmentFallback = domainInherentLabel
    )

    private val memberId = FallbackField(
        fieldName = "MemberId"
    )

    private val memberInherentLabel = FallbackField(
        fieldName = "MemberLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(memberId, memberInherentLabel)
    )

    private val memberCode = KeySegmentField(
        fieldName = "MemberCode",
        segmentKind = KeySegmentKind.TOP_LEVEL_SEGMENT,
        segmentFallback = memberInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "MemberLabel"
    )

    private val isDefaultMember = AtomField(
        fieldName = "IsDefaultMember"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Member",
        sectionTitle = "Members",
        sectionDescription = "Members: DefaultMember assignment changes",
        sectionFields = listOf(
            domainInherentLabel,
            domainCode,
            memberId,
            memberInherentLabel,
            recordIdentityFallback,
            memberCode,
            *identificationLabels,
            changeKind,
            isDefaultMember,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(domainCode),
            NumberAwareSort(memberCode),
            FixedChangeKindSort(changeKind)
        ),
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "DomainInherentLabel" to domainInherentLabel,
        "DomainCode" to domainCode,
        "MemberId" to memberId,
        "MemberInherentLabel" to memberInherentLabel,
        "MemberCode" to memberCode,
        *idLabelColumnMapping(),
        "IsDefaultMember" to isDefaultMember
    )

    override val query = """
        SELECT
        mDomain.DomainLabel AS 'DomainInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
        ,mMember.MemberID AS 'MemberId'
        ,mMember.MemberLabel AS 'MemberInherentLabel'
        ,mMember.MemberCode AS 'MemberCode'
        ${idLabelAggregateFragment()}
        ,mMember.IsDefaultMember AS 'IsDefaultMember'

        FROM mMember
        LEFT JOIN mDomain ON mDomain.DomainID = mMember.DomainID
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mMember.ConceptID
        LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID

        WHERE
        (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)
        AND mMember.MemberID NOT IN (SELECT CorrespondingMemberID FROM mMetric)

        GROUP BY mMember.MemberID

        ORDER BY mDomain.DomainCode ASC, mMember.MemberCode ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        SourceTableDescriptor(
            table = "mMember",
            where = "mMember.MemberID NOT IN (SELECT CorrespondingMemberID FROM mMetric)"
        )
    )

    init {
        sanityCheckSectionConfig()
    }
}
