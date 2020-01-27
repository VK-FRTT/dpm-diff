package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndBlankLines
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
    private val domainCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_ID,
        fieldName = "domain code"
    )

    private val memberCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_ID,
        fieldName = "member code"
    )

    override val discriminationLabels = composeDiscriminationLabelFields {
        "member label $it"
    }

    private val isDefaultMember = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "is default member"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Members",
        sectionTitle = "Members",
        sectionDescription = "Members: DefaultMember assignment changes",
        sectionFields = listOf(
            domainCode,
            memberCode,
            *discriminationLabels,
            differenceKind,
            isDefaultMember
        )
    )

    override val query = """
        SELECT
        mDomain.DomainCode AS 'DomainCode'
        ,mMember.MemberCode AS 'MemberCode'
        ,mMember.IsDefaultMember AS 'IsDefaultMember'
        ${composeDiscriminationLabelQueryFragment()}

        FROM mMember
        LEFT JOIN mDomain on mDomain.DomainID = mMember.DomainID
        LEFT JOIN mConceptTranslation on mConceptTranslation.ConceptID = mMember.ConceptID
        LEFT JOIN mLanguage on mConceptTranslation.LanguageID = mLanguage.LanguageID

        WHERE
        mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

        GROUP BY mDomain.DomainCode, mMember.MemberCode
    """.trimLineStartsAndBlankLines()

    override val queryPrimaryTables = listOf("mMember")

    override val columnNames = mapOf(
        "DomainCode" to domainCode,
        "MemberCode" to memberCode,
        *composeDiscriminationLabelColumnNames(),
        "IsDefaultMember" to isDefaultMember
    )
}
