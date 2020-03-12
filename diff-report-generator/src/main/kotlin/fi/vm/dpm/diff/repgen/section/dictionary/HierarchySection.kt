package fi.vm.dpm.diff.repgen.section.dictionary

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.CorrelationKeyField
import fi.vm.dpm.diff.model.CorrelationKeyKind
import fi.vm.dpm.diff.model.CorrelationMode
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.SectionBase

class HierarchySection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val hierarchyId = FallbackField(
        fieldName = "HierarchyId"
    )

    private val hierarchyInherentLabel = FallbackField(
        fieldName = "HierarchyLabel"
    )

    private val domainInherentLabel = FallbackField(
        fieldName = "DomainLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(hierarchyId, hierarchyInherentLabel)
    )

    private val domainCode = CorrelationKeyField(
        fieldName = "DomainCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = domainInherentLabel
    )

    private val hierarchyCode = CorrelationKeyField(
        fieldName = "HierarchyCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = hierarchyInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "HierarchyLabel"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Hierarchy",
        sectionTitle = "Hierarchies",
        sectionDescription = "Hierarchies: Domain reference changes",
        sectionFields = listOf(
            hierarchyId,
            hierarchyInherentLabel,
            recordIdentityFallback,
            domainInherentLabel,
            domainCode,
            hierarchyCode,
            *identificationLabels,
            changeKind,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(domainCode),
            NumberAwareSort(hierarchyCode),
            FixedChangeKindSort(changeKind)
        ),
        correlationMode = CorrelationMode.ONE_PHASE_BY_FULL_KEY,
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "HierarchyId" to hierarchyId,
        "HierarchyInherentLabel" to hierarchyInherentLabel,
        "DomainInherentLabel" to domainInherentLabel,
        "DomainCode" to domainCode,
        "HierarchyCode" to hierarchyCode,
        *idLabelColumnMapping()
    )

    override val query = """
        SELECT
        mHierarchy.HierarchyID AS 'HierarchyId'
        ,mHierarchy.HierarchyLabel AS 'HierarchyInherentLabel'
        ,mDomain.DomainLabel AS 'DomainInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
        ,mHierarchy.HierarchyCode AS 'HierarchyCode'
        ${idLabelAggregateFragment()}

        FROM mHierarchy
        LEFT JOIN mDomain on mDomain.DomainID = mHierarchy.DomainID
        LEFT JOIN mConceptTranslation on mConceptTranslation.ConceptID = mHierarchy.ConceptID
        LEFT JOIN mLanguage on mLanguage.LanguageID = mConceptTranslation.LanguageID

        WHERE
        mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

        GROUP BY mHierarchy.HierarchyID

        ORDER BY mDomain.DomainCode ASC, mHierarchy.HierarchyCode ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        "mHierarchy"
    )

    init {
        sanityCheckSectionConfig()
    }
}
