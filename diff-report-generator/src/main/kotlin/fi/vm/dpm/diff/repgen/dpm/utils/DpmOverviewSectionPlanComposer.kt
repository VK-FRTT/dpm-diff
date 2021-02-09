package fi.vm.dpm.diff.repgen.dpm.utils

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.ChangeDetectionMode
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.FixedChangeKindSortBy
import fi.vm.dpm.diff.model.FixedElementTypeSortBy
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.NumberAwareSortBy
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions

class DpmOverviewSectionPlanComposer(
    private val dpmElementQueryDescriptors: List<DpmElementQueryDescriptor>,
    dpmSectionOptions: DpmSectionOptions
) {
    val elementId = FallbackField(
        fieldName = "ElementId"
    )

    val elementInherentLabel = FallbackField(
        fieldName = "ElementLabel"
    )

    val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(elementId, elementInherentLabel)
    )

    val parentElementType = KeyField(
        fieldName = "ParentElementType",
        keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
        keyFieldFallback = null
    )

    val parentElementCode = KeyField(
        fieldName = "ParentElementCode",
        keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
        keyFieldFallback = null
    )

    val elementType = KeyField(
        fieldName = "ElementType",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    val elementCode = KeyField(
        fieldName = "ElementCode",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = elementInherentLabel
    )

    val identificationLabels = DpmSectionIdentificationLabels(
        fieldNameBase = "ElementLabel",
        dpmSectionOptions = dpmSectionOptions
    )
    val changeKind = ChangeKindField()

    val note = NoteField()

    fun sectionOutline(
        sectionShortTitle: String,
        sectionTitle: String,
        sectionDescription: String
    ): SectionOutline {
        return SectionOutline(
            sectionShortTitle = sectionShortTitle,
            sectionTitle = sectionTitle,
            sectionDescription = sectionDescription,
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                elementId,
                elementInherentLabel,
                recordIdentityFallback,
                parentElementType,
                parentElementCode,
                elementType,
                elementCode,
                *identificationLabels.labelFields(),
                changeKind,
                note
            ),
            sectionSortOrder = listOf(
                FixedElementTypeSortBy(parentElementType),
                NumberAwareSortBy(parentElementCode),
                FixedElementTypeSortBy(elementType),
                NumberAwareSortBy(elementCode),
                FixedChangeKindSortBy(changeKind)
            ),
            includedChanges = setOf(
                ChangeKind.ADDED,
                ChangeKind.DELETED
            )
        )
    }

    fun queryColumnMapping(): Map<String, Field> {
        return mapOf(
            "ElementId" to elementId,
            "ElementInherentLabel" to elementInherentLabel,
            "ElementType" to elementType,
            "ElementCode" to elementCode,
            "ParentElementType" to parentElementType,
            "ParentElementCode" to parentElementCode,
            *identificationLabels.labelColumnMapping()
        )
    }

    fun query(): String {
        val query =
            """
            -- Shared sub-queries
            WITH ${dpmElementQueryDescriptors
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
            ${identificationLabels.labelColumnNamesFragment()}

            FROM (
                ${dpmElementQueryDescriptors
                .map { "SELECT * FROM ${elementOverviewQueryName(it)}" }
                .joinToString("\nUNION ALL\n")}
            )

            ORDER BY ElementType, ParentElementCode, ElementCode
            """

        return query.trimLineStartsAndConsequentBlankLines()
    }

    fun elementOverviewQueryExpression(
        dpmElementQueryQueryDescriptor: DpmElementQueryDescriptor
    ): String {
        return with(dpmElementQueryQueryDescriptor) {
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
             ${identificationLabels.labelAggregateFragment()}

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

    fun elementOverviewQueryName(
        dpmElementQueryQueryDescriptor: DpmElementQueryDescriptor
    ): String {
        return "${dpmElementQueryQueryDescriptor.elementType}Overview"
    }
}
