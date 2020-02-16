package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.CorrelationKeyKind
import fi.vm.dpm.diff.model.CorrelationMode
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
    private val dimensionId = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "DimensionId"
    )

    private val dimensionInherentLabel = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "DimensionLabel"
    )

    private val dimensionCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "DimensionCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = dimensionInherentLabel,
        noteFields = listOf(dimensionId, dimensionInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "DimensionLabel",
        noteField = dimensionInherentLabel
    )

    private val referencedDomainCode = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "ReferencedDomainCode"
    )

    private val isTypedDimension = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "IsTypedDimension"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Dimension",
        sectionTitle = "Dimensions",
        sectionDescription = "Dimensions: Domain reference and dimension kind changes",
        sectionFields = listOf(
            dimensionId,
            dimensionInherentLabel,
            dimensionCode,
            *identificationLabels,
            changeKind,
            referencedDomainCode,
            isTypedDimension,
            note
        ),
        correlationMode = CorrelationMode.ONE_PHASE_BY_FULL_KEY,
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "DimensionId" to dimensionId,
        "DimensionInherentLabel" to dimensionInherentLabel,
        "DimensionCode" to dimensionCode,
        *idLabelColumnMapping(),
        "DomainCode" to referencedDomainCode,
        "IsTypedDimension" to isTypedDimension
    )

    override val query = """
        SELECT
        mDimension.DimensionID AS 'DimensionId'
        ,mDimension.DimensionLabel AS 'DimensionInherentLabel'
        ,mDimension.DimensionCode AS 'DimensionCode'
        ${idLabelAggregateFragment()}
        ,mDomain.DomainCode AS 'DomainCode'
        ,mDimension.IsTypedDimension AS 'IsTypedDimension'

        FROM mDimension
        LEFT JOIN mDomain ON mDomain.DomainID = mDimension.DomainID
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mDimension.ConceptID
        LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID

        WHERE
        mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

        GROUP BY mDimension.DimensionID

        ORDER BY mDomain.DomainCode ASC, mDimension.DimensionCode ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        "mDimension"
    )

    init {
        sanityCheckSectionConfig()
    }
}
