package fi.vm.dpm.diff.repgen.dpm.section.dictionary

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
import fi.vm.dpm.diff.repgen.dpm.DpmGenerationContext
import fi.vm.dpm.diff.repgen.dpm.section.SectionBase

class DomainSection(
    generationContext: DpmGenerationContext
) : SectionBase(
    generationContext
) {
    private val domainId = FallbackField(
        fieldName = "DomainId"
    )

    private val domainInherentLabel = FallbackField(
        fieldName = "DomainLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(domainId, domainInherentLabel)
    )

    private val domainCode = KeySegmentField(
        fieldName = "DomainCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = domainInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "DomainLabel"
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
        sectionDescription = "Added and deleted Domains, changes in IsTypedDomain and DataType",
        sectionFields = listOf(
            domainId,
            domainInherentLabel,
            recordIdentityFallback,
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
        includedChanges = ChangeKind.allChanges()
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
