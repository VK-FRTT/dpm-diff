package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.DifferenceKind
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
    override val includedDifferenceKinds: Array<DifferenceKind> = DifferenceKind.values()

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
        correlationKeyFallback = hierarchyInherentLabel,
        noteFallback = listOf(hierarchyId, hierarchyInherentLabel)
    )

    private val hierarchyCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "HierarchyCode",
        correlationKeyFallback = hierarchyInherentLabel,
        noteFallback = listOf(hierarchyId, hierarchyInherentLabel)
    )

    override val identificationLabels = composeIdentificationLabelFields(
        noteFallback = hierarchyInherentLabel
    ) {
        "HierarchyLabel$it"
    }

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
            differenceKind,
            note
        )
    )

    override val queryColumnMapping = mapOf(
        "HierarchyId" to hierarchyId,
        "HierarchyInherentLabel" to hierarchyInherentLabel,
        "DomainCode" to domainCode,
        "HierarchyCode" to hierarchyCode,
        *composeIdentificationLabelColumnNames()
    )

    override val query = """
        SELECT
        mHierarchy.HierarchyID AS 'HierarchyId'
        ,mHierarchy.HierarchyLabel AS 'HierarchyInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
        ,mHierarchy.HierarchyCode AS 'HierarchyCode'
        ${composeIdentificationLabelQueryFragment("mLanguage.IsoCode", "mConceptTranslation.Text")}

        FROM mHierarchy
        LEFT JOIN mDomain on mDomain.DomainID = mHierarchy.DomainID
        LEFT JOIN mConceptTranslation on mConceptTranslation.ConceptID = mHierarchy.ConceptID
        LEFT JOIN mLanguage on mLanguage.LanguageID = mConceptTranslation.LanguageID

        WHERE
        mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

        GROUP BY mHierarchy.HierarchyID

        ORDER BY mDomain.DomainCode ASC, mHierarchy.HierarchyCode ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val primaryTables = listOf(
        "mHierarchy"
    )

    init {
        sanityCheckSectionConfig()
    }
}
