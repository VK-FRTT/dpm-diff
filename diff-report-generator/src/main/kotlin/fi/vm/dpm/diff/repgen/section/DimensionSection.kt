package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.CorrelationKeyField
import fi.vm.dpm.diff.model.CorrelationKeyKind
import fi.vm.dpm.diff.model.CorrelationMode
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class DimensionSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val dimensionId = FallbackField(
        fieldName = "DimensionId"
    )

    private val dimensionInherentLabel = FallbackField(
        fieldName = "DimensionLabel"
    )

    private val dimensionCode = CorrelationKeyField(
        fieldName = "DimensionCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = dimensionInherentLabel,
        noteFallbacks = listOf(dimensionId, dimensionInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "DimensionLabel",
        fallbackField = dimensionInherentLabel
    )

    private val referencedDomainCode = AtomField(
        fieldName = "ReferencedDomainCode"
    )

    private val isTypedDimension = AtomField(
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
