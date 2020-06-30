package fi.vm.dpm.diff.repgen.section.dictionary

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.KeySegmentField
import fi.vm.dpm.diff.model.KeySegmentKind
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

    private val domainInherentLabel = FallbackField(
        fieldName = "DomainLabel"
    )

    private val domainCode = KeySegmentField(
        fieldName = "DomainCode",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = domainInherentLabel
    )

    private val hierarchyId = FallbackField(
        fieldName = "HierarchyId"
    )

    private val hierarchyInherentLabel = FallbackField(
        fieldName = "HierarchyLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(hierarchyId, hierarchyInherentLabel)
    )

    private val hierarchyCode = KeySegmentField(
        fieldName = "HierarchyCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = hierarchyInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "HierarchyLabel"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Hierarchy",
        sectionTitle = "Hierarchies",
        sectionDescription = "Hierarchies: Domain reference changes",
        sectionFields = listOf(
            domainInherentLabel,
            domainCode,
            hierarchyId,
            hierarchyInherentLabel,
            recordIdentityFallback,
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
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "DomainInherentLabel" to domainInherentLabel,
        "DomainCode" to domainCode,
        "HierarchyId" to hierarchyId,
        "HierarchyInherentLabel" to hierarchyInherentLabel,
        "HierarchyCode" to hierarchyCode,
        *idLabelColumnMapping()
    )

    override val query = """
        SELECT
        mDomain.DomainLabel AS 'DomainInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
        ,mHierarchy.HierarchyID AS 'HierarchyId'
        ,mHierarchy.HierarchyLabel AS 'HierarchyInherentLabel'
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
