package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.CorrelationMode
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext

class DictionaryElementOverviewSection(
    generationContext: GenerationContext
) : DictionarySectionBase(
    generationContext
) {
    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "DictElemOverview",
        sectionTitle = "Dictionary elements overview",
        sectionDescription = "Dictionary elements: added and deleted Domains, Members, Metrics, Dimensions and Hierarchies",
        sectionFields = listOf(
            elementId,
            elementInherentLabel,
            elementType,
            elementCode,
            parentElementCode,
            *identificationLabels,
            changeKind,
            note
        ),
        correlationMode = CorrelationMode.ONE_PHASE_BY_FULL_KEY,
        includedChanges = setOf(
            ChangeKind.ADDED,
            ChangeKind.DELETED
        )
    )

    override val queryColumnMapping = mapOf(
        "ElementId" to elementId,
        "ElementInherentLabel" to elementInherentLabel,
        "ElementType" to elementType,
        "ElementCode" to elementCode,
        "ParentElementCode" to parentElementCode,
        *idLabelColumnMapping()
    )

    override val query = run {

        val query =
            """
            -- Shared sub-queries
            WITH ${elementQueryDescriptors
                .map(::elementEssentialsQueryExpression)
                .joinToString(",\n\n")
            }

            -- Main query
            SELECT
            ElementId AS ElementId
            ,ElementInherentLabel AS ElementInherentLabel
            ,ElementType AS ElementType
            ,ElementCode AS ElementCode
            ,ParentElementCode AS ParentElementCode
            ${idLabelColumnNamesFragment()}

            FROM (
                ${elementQueryDescriptors
                .map { "SELECT * FROM ${elementEssentialsQueryName(it)}" }
                .joinToString("\nUNION ALL\n")}
            )

            ORDER BY ElementType, ParentElementCode, ElementCode
            """

        query.trimLineStartsAndConsequentBlankLines()
    }

    override val sourceTableDescriptors = listOf(
        "mDomain",
        "mMember",
        "mDimension",
        "mHierarchy"
    )

    init {
        sanityCheckSectionConfig()
    }
}
