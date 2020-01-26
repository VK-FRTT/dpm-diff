package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndBlankLines
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class DimensionSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val dimensionCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_ID,
        fieldName = "dimension code"
    )

    override val discriminationLabels = composeDiscriminationLabelFields {
        "dimension label $it"
    }

    private val referencedDomainCode = FieldDescriptor(
        fieldKind = FieldKind.CHANGE,
        fieldName = "referenced domain code"
    )

    private val isTypedDimension = FieldDescriptor(
        fieldKind = FieldKind.CHANGE,
        fieldName = "is typed dimension"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Dimensions",
        sectionTitle = "Dimensions",
        sectionDescription = "Dimensions: Domain reference and dimension kind changes",
        sectionFields = listOf(
            dimensionCode,
            *discriminationLabels,
            differenceKind,
            referencedDomainCode,
            isTypedDimension
        )
    )

    override val query = """
        SELECT
        mDimension.DimensionCode AS 'DimensionCode'
        ,mDomain.DomainCode AS 'DomainCode'
        ,mDimension.IsTypedDimension AS 'IsTypedDimension'
        ${composeDiscriminationLabelQueryFragment()}

        FROM mDimension
        LEFT JOIN mDomain on mDomain.DomainID = mDimension.DomainID
        LEFT JOIN mConceptTranslation on mConceptTranslation.ConceptID = mDimension.ConceptID
        LEFT JOIN mLanguage on mConceptTranslation.LanguageID = mLanguage.LanguageID

        WHERE
        mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

        GROUP BY mDimension.DimensionCode
    """.trimLineStartsAndBlankLines()

    override val queryPrimaryTables = listOf("mDimension")

    override val columnNames = mapOf(
        "DimensionCode" to dimensionCode,
        *composeDiscriminationLabelColumnNames(),
        "DomainCode" to referencedDomainCode,
        "IsTypedDimension" to isTypedDimension
    )
}
