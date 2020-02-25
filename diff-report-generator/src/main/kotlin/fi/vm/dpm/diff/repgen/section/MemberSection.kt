package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.CorrelationKeyField
import fi.vm.dpm.diff.model.CorrelationKeyKind
import fi.vm.dpm.diff.model.CorrelationMode
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class MemberSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val memberId = FallbackField(
        fieldName = "MemberId"
    )

    private val memberInherentLabel = FallbackField(
        fieldName = "MemberLabel"
    )

    private val domainCode = CorrelationKeyField(
        fieldName = "DomainCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = memberInherentLabel,
        noteFallbacks = listOf(memberId, memberInherentLabel)
    )

    private val memberCode = CorrelationKeyField(
        fieldName = "MemberCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = memberInherentLabel,
        noteFallbacks = listOf(memberId, memberInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "MemberLabel",
        fallbackField = memberInherentLabel
    )

    private val isDefaultMember = AtomField(
        fieldName = "IsDefaultMember"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Member",
        sectionTitle = "Members",
        sectionDescription = "Members: DefaultMember assignment changes",
        sectionFields = listOf(
            memberId,
            memberInherentLabel,
            domainCode,
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
        correlationMode = CorrelationMode.ONE_PHASE_BY_FULL_KEY,
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "MemberId" to memberId,
        "MemberInherentLabel" to memberInherentLabel,
        "DomainCode" to domainCode,
        "MemberCode" to memberCode,
        *idLabelColumnMapping(),
        "IsDefaultMember" to isDefaultMember
    )

    override val query = """
        SELECT
        mMember.MemberID AS 'MemberId'
        ,mMember.MemberLabel AS 'MemberInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
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
