package fi.vm.dpm.diff.repgen.section.dictionary

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyKind
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.SectionBase

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

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(dimensionId, dimensionInherentLabel)
    )

    private val dimensionCode = KeyField(
        fieldName = "DimensionCode",
        keyKind = KeyKind.PRIMARY_KEY,
        keyFallback = dimensionInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "DimensionLabel"
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
            recordIdentityFallback,
            dimensionCode,
            *identificationLabels,
            changeKind,
            referencedDomainCode,
            isTypedDimension,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(dimensionCode),
            FixedChangeKindSort(changeKind)
        ),
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