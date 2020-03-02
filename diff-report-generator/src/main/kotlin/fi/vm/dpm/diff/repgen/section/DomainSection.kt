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

class DomainSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val domainId = FallbackField(
        fieldName = "DomainId"
    )

    private val domainInherentLabel = FallbackField(
        fieldName = "DomainLabel"
    )

    private val domainCode = CorrelationKeyField(
        fieldName = "DomainCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = domainInherentLabel,
        noteFallbacks = listOf(domainId, domainInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "DomainLabel",
        fallbackField = domainInherentLabel
    )

    private val dataType = AtomField(
        fieldName = "TypedDomainDataType"
    )

    private val isTypedDomain = AtomField(
        fieldName = "IsTypedDomain"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Domain",
        sectionTitle = "Domains",
        sectionDescription = "Domains: data type changes (in TypedDomains) and domain kind changes (TypedDomain/ExplicitDomain)",
        sectionFields = listOf(
            domainId,
            domainInherentLabel,
            domainCode,
            *identificationLabels,
            changeKind,
            isTypedDomain,
            dataType,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(domainCode),
            FixedChangeKindSort(changeKind)
        ),
        correlationMode = CorrelationMode.ONE_PHASE_BY_FULL_KEY,
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "DomainId" to domainId,
        "DomainInherentLabel" to domainInherentLabel,
        "DomainCode" to domainCode,
        *idLabelColumnMapping(),
        "DataType" to dataType,
        "IsTypedDomain" to isTypedDomain
    )

    override val query = """
        SELECT
        mDomain.DomainID AS 'DomainId'
        ,mDomain.DomainLabel AS 'DomainInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
        ${idLabelAggregateFragment()}
        ,mDomain.DataType AS 'DataType'
        ,mDomain.IsTypedDomain AS 'IsTypedDomain'

        FROM mDomain
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mDomain.ConceptID
        LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID

        WHERE
        mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

        GROUP BY mDomain.DomainID

        ORDER BY mDomain.DomainCode ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        "mDomain"
    )

    init {
        sanityCheckSectionConfig()
    }
}
