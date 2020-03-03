package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.CorrelationKeyField
import fi.vm.dpm.diff.model.CorrelationKeyKind
import fi.vm.dpm.diff.model.CorrelationMode
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.FixedElementTypeSort
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext

open class ElementOverviewSectionBase(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    protected val elementId = FallbackField(
        fieldName = "ElementId"
    )

    protected val elementInherentLabel = FallbackField(
        fieldName = "ElementLabel"
    )

    protected val parentElementType = CorrelationKeyField(
        fieldName = "ParentElementType",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = null,
        noteFallbacks = emptyList()
    )

    protected val parentElementCode = CorrelationKeyField(
        fieldName = "ParentElementCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = null,
        noteFallbacks = emptyList()
    )

    protected val elementType = CorrelationKeyField(
        fieldName = "ElementType",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = null,
        noteFallbacks = emptyList()
    )

    protected val elementCode = CorrelationKeyField(
        fieldName = "ElementCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = elementInherentLabel,
        noteFallbacks = listOf(elementId, elementInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "ElementLabel",
        fallbackField = elementInherentLabel
    )

    protected fun elementOverviewSectionDescriptor(
        sectionShortTitle: String,
        sectionTitle: String,
        sectionDescription: String
    ): SectionDescriptor {
        return SectionDescriptor(
            sectionShortTitle = sectionShortTitle,
            sectionTitle = sectionTitle,
            sectionDescription = sectionDescription,
            sectionFields = listOf(
                elementId,
                elementInherentLabel,
                parentElementType,
                parentElementCode,
                elementType,
                elementCode,
                *identificationLabels,
                changeKind,
                note
            ),
            sectionSortOrder = listOf(
                FixedElementTypeSort(parentElementType),
                NumberAwareSort(parentElementCode),
                FixedElementTypeSort(elementType),
                NumberAwareSort(elementCode),
                FixedChangeKindSort(changeKind)
            ),
            correlationMode = CorrelationMode.ONE_PHASE_BY_FULL_KEY,
            includedChanges = setOf(
                ChangeKind.ADDED,
                ChangeKind.DELETED
            )
        )
    }

    protected fun elementOverviewQueryColumnMappings(): Map<String, Field> {
        return mapOf(
            "ElementId" to elementId,
            "ElementInherentLabel" to elementInherentLabel,
            "ElementType" to elementType,
            "ElementCode" to elementCode,
            "ParentElementType" to parentElementType,
            "ParentElementCode" to parentElementCode,
            *idLabelColumnMapping()
        )
    }

    protected fun elementOverviewQuery(elementQueryDescriptors: List<ElementQueryDescriptor>): String {
        val query =
            """
            -- Shared sub-queries
            WITH ${elementQueryDescriptors
                .map(::elementOverviewQueryExpression)
                .joinToString(",\n\n")
            }

            -- Main query
            SELECT
            ElementId AS ElementId
            ,ElementInherentLabel AS ElementInherentLabel
            ,ElementType AS ElementType
            ,ElementCode AS ElementCode
            ,ParentElementType AS ParentElementType
            ,ParentElementCode AS ParentElementCode
            ${idLabelColumnNamesFragment()}

            FROM (
                ${elementQueryDescriptors
                .map { "SELECT * FROM ${elementOverviewQueryName(it)}" }
                .joinToString("\nUNION ALL\n")}
            )

            ORDER BY ElementType, ParentElementCode, ElementCode
            """

        return query.trimLineStartsAndConsequentBlankLines()
    }

    protected fun elementOverviewQueryExpression(
        elementQueryDescriptor: ElementQueryDescriptor
    ): String {
        return with(elementQueryDescriptor) {
            """
            ${elementOverviewQueryName(this)} AS (
            SELECT
            '$elementType' AS ElementType
            ,$elementTableName.$elementIdColumn AS ElementId
            ,$elementTableName.ConceptID AS ElementConceptId
            ,$elementTableName.$elementInherentLabelColumn AS ElementInherentLabel
            ,$elementTableName.$elementCodeColumn AS ElementCode
            ,'$parentType' AS ParentElementType
            ,$parentCodeStatement AS ParentElementCode
            ${idLabelAggregateFragment()}

            FROM
            $elementTableName

            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = $elementTableName.ConceptID
            LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID
            $parentTableJoin

            WHERE
            (mConceptTranslation.Role = 'label' OR mConceptTranslation.Role IS NULL)
            $elementTableSliceCriteria

            GROUP BY $elementTableName.$elementIdColumn
            )
            """.trimLineStartsAndConsequentBlankLines()
        }
    }

    protected fun elementOverviewQueryName(
        elementQueryDescription: ElementQueryDescriptor
    ): String {
        return "${elementQueryDescription.elementType}Overview"
    }
}
