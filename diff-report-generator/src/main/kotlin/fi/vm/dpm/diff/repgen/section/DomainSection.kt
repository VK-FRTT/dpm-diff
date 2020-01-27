package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class DomainSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val domainCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_ID,
        fieldName = "domain code"
    )

    override val identificationLabels = composeIdentificationLabels {
        "domain label $it"
    }

    private val dataType = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "data type"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Domains",
        sectionTitle = "Domains",
        sectionDescription = "Domains: DataType changes",
        sectionFields = listOf(
            domainCode,
            *identificationLabels,
            differenceKind,
            dataType
        )
    )

    override val query = """
        SELECT
        mDomain.DomainCode AS 'DomainCode'
        ,mDomain.DataType AS 'DataType'
        ${composeIdentificationLabelQueryFragment("mLanguage.IsoCode", "mConceptTranslation.Text")}

        FROM mDomain
        LEFT JOIN mConceptTranslation on mConceptTranslation.ConceptID = mDomain.ConceptID
        LEFT JOIN mLanguage on mConceptTranslation.LanguageID = mLanguage.LanguageID

        WHERE
        mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

        GROUP BY mDomain.DomainCode
    """.trimLineStartsAndConsequentBlankLines()

    override val queryPrimaryTables = listOf("mDomain")

    override val columnNames = mapOf(
        "DomainCode" to domainCode,
        *composeIdentificationLabelColumnNames(),
        "DataType" to dataType
    )
}
