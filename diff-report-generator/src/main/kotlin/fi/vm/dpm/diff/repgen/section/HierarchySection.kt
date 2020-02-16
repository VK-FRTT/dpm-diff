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

class HierarchySection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val hierarchyId = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "HierarchyId"
    )

    private val hierarchyInherentLabel = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "HierarchyLabel"
    )

    private val domainCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "DomainCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = hierarchyInherentLabel,
        noteFields = listOf(hierarchyId, hierarchyInherentLabel)
    )

    private val hierarchyCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "HierarchyCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = hierarchyInherentLabel,
        noteFields = listOf(hierarchyId, hierarchyInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "HierarchyLabel",
        noteField = hierarchyInherentLabel
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Hierarchy",
        sectionTitle = "Hierarchies",
        sectionDescription = "Hierarchies: Domain reference changes",
        sectionFields = listOf(
            hierarchyId,
            hierarchyInherentLabel,
            domainCode,
            hierarchyCode,
            *identificationLabels,
            changeKind,
            note
        ),
        correlationMode = CorrelationMode.ONE_PHASE_BY_FULL_KEY,
        includedChanges = ChangeKind.allValues()
    )

    override val queryColumnMapping = mapOf(
        "HierarchyId" to hierarchyId,
        "HierarchyInherentLabel" to hierarchyInherentLabel,
        "DomainCode" to domainCode,
        "HierarchyCode" to hierarchyCode,
        *idLabelColumnMapping()
    )

    override val query = """
        SELECT
        mHierarchy.HierarchyID AS 'HierarchyId'
        ,mHierarchy.HierarchyLabel AS 'HierarchyInherentLabel'
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
