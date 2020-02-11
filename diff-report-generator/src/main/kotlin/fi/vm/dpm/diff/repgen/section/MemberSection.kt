package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.DifferenceKind
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class MemberSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    override val includedDifferenceKinds: Array<DifferenceKind> = DifferenceKind.values()

    private val memberId = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "MemberId"
    )

    private val memberInherentLabel = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "MemberLabel"
    )

    private val domainCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "DomainCode",
        correlationKeyFallback = memberInherentLabel,
        noteFallback = listOf(memberId, memberInherentLabel)
    )

    private val memberCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "MemberCode",
        correlationKeyFallback = memberInherentLabel,
        noteFallback = listOf(memberId, memberInherentLabel)
    )

    override val identificationLabels = composeIdentificationLabelFields(
        noteFallback = memberInherentLabel
    ) {
        "MemberLabel$it"
    }

    private val isDefaultMember = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
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
            differenceKind,
            isDefaultMember,
            note
        )
    )

    override val queryColumnMapping = mapOf(
        "MemberId" to memberId,
        "MemberInherentLabel" to memberInherentLabel,
        "DomainCode" to domainCode,
        "MemberCode" to memberCode,
        *composeIdentificationLabelColumnNames(),
        "IsDefaultMember" to isDefaultMember
    )

    override val query = """
        SELECT
        mMember.MemberID AS 'MemberId'
        ,mMember.MemberLabel AS 'MemberInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
        ,mMember.MemberCode AS 'MemberCode'
        ${composeIdentificationLabelQueryFragment("mLanguage.IsoCode", "mConceptTranslation.Text")}
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

    override val primaryTables = listOf(
        Pair("mMember", "mMember.MemberID NOT IN (SELECT CorrespondingMemberID FROM mMetric)")
    )

    init {
        sanityCheckSectionConfig()
    }
}
