package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.FixedElementTypeSort
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyKind
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
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

    protected val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(elementId, elementInherentLabel)
    )

    protected val parentElementType = KeyField(
        fieldName = "ParentElementType",
        keyKind = KeyKind.PRIMARY_SCOPE_KEY,
        keyFallback = null
    )

    protected val parentElementCode = KeyField(
        fieldName = "ParentElementCode",
        keyKind = KeyKind.PRIMARY_SCOPE_KEY,
        keyFallback = null
    )

    protected val elementType = KeyField(
        fieldName = "ElementType",
        keyKind = KeyKind.PRIMARY_KEY,
        keyFallback = null
    )

    protected val elementCode = KeyField(
        fieldName = "ElementCode",
        keyKind = KeyKind.PRIMARY_KEY,
        keyFallback = elementInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "ElementLabel"
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
                recordIdentityFallback,
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
