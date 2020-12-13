package fi.vm.dpm.diff.repgen.dpm.utils

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.AtomOption
import fi.vm.dpm.diff.model.ChangeDetectionMode
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.DisplayHint
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.FixedElementTypeSort
import fi.vm.dpm.diff.model.FixedTranslationRoleSort
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions

class DpmTranslationSectionPlanComposer(
    private val dpmElementQueryDescriptors: List<DpmElementQueryDescriptor>,
    private val dpmSectionOptions: DpmSectionOptions
) {
    private val overviewSectionPlanComposer =
        DpmOverviewSectionPlanComposer(
            dpmElementQueryDescriptors,
            dpmSectionOptions
        )

    private val elementType = KeyField(
        fieldName = "ElementType",
        keyFieldKind = KeyFieldKind.PARENT_KEY,
        keyFieldFallback = null
    )

    private val elementCode = KeyField(
        fieldName = "ElementCode",
        keyFieldKind = KeyFieldKind.PARENT_KEY,
        keyFieldFallback = overviewSectionPlanComposer.elementInherentLabel
    )

    private val translationRole = KeyField(
        fieldName = "TranslationRole",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private val translationLanguage = KeyField(
        fieldName = "Language",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private val translation = AtomField(
        fieldName = "Translation",
        displayHint = DisplayHint.FIXED_EXTRA_WIDE,
        atomOptions = listOf(AtomOption.OUTPUT_TO_ADDED_CHANGE)
    )

    fun sectionOutline(
        sectionShortTitle: String,
        sectionTitle: String,
        sectionDescription: String
    ): SectionOutline {
        return SectionOutline(
            sectionShortTitle = sectionShortTitle,
            sectionTitle = sectionTitle,
            sectionDescription = sectionDescription,
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE,
            sectionFields = listOf(
                overviewSectionPlanComposer.elementId,
                overviewSectionPlanComposer.elementInherentLabel,
                overviewSectionPlanComposer.recordIdentityFallback,
                overviewSectionPlanComposer.parentElementType,
                overviewSectionPlanComposer.parentElementCode,
                elementType,
                elementCode,
                *overviewSectionPlanComposer.identificationLabels.labelFields(),
                translationRole,
                translationLanguage,
                overviewSectionPlanComposer.changeKind,
                translation,
                overviewSectionPlanComposer.note
            ),
            sectionSortOrder = listOf(
                FixedElementTypeSort(overviewSectionPlanComposer.parentElementType),
                NumberAwareSort(overviewSectionPlanComposer.parentElementCode),
                FixedElementTypeSort(elementType),
                NumberAwareSort(elementCode),
                FixedTranslationRoleSort(translationRole),
                NumberAwareSort(translationLanguage),
                FixedChangeKindSort(overviewSectionPlanComposer.changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )
    }

    fun queryColumnMapping(): Map<String, Field> {
        return mapOf(
            "ElementId" to overviewSectionPlanComposer.elementId,
            "ElementInherentLabel" to overviewSectionPlanComposer.elementInherentLabel,
            "ElementType" to elementType,
            "ElementCode" to elementCode,
            "ParentElementType" to overviewSectionPlanComposer.parentElementType,
            "ParentElementCode" to overviewSectionPlanComposer.parentElementCode,
            *overviewSectionPlanComposer.identificationLabels.labelColumnMapping(),
            "TranslationRole" to translationRole,
            "TranslationLanguage" to translationLanguage,
            "Translation" to translation
        )
    }

    fun query(): String {

        val translationLangsOptionHelper = TranslationLangsOptionHelper(dpmSectionOptions)

        val query =
            """
            -- Shared sub-queries
            WITH ${dpmElementQueryDescriptors
                .map {
                    """
                        ${overviewSectionPlanComposer.elementOverviewQueryExpression(it)},
                        ${"\n" + elementTranslationQueryExpression(it)}
                    """.trimLineStartsAndConsequentBlankLines()
                }
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
            ${overviewSectionPlanComposer.identificationLabels.labelColumnNamesFragment()}
            ,TranslationRole AS TranslationRole
            ,TranslationLanguage AS TranslationLanguage
            ,Translation AS Translation

            FROM (
                ${dpmElementQueryDescriptors
                .map { "SELECT * FROM ${elementTranslationQueryName(it)}" }
                .joinToString("\nUNION ALL\n")}
            )

            ${translationLangsOptionHelper.translationLanguageWhereStatement()}

            ORDER BY ElementType, ParentElementCode, ElementCode
            """

        return query.trimLineStartsAndConsequentBlankLines()
    }

    private fun elementTranslationQueryExpression(
        dpmElementQueryQueryDescriptor: DpmElementQueryDescriptor
    ): String {
        return """
            ${elementTranslationQueryName(dpmElementQueryQueryDescriptor)} AS (
            SELECT
            ElementType AS ElementType
            ,ElementId AS ElementId
            ,ElementInherentLabel AS ElementInherentLabel
            ,ElementCode AS ElementCode
            ,ParentElementType AS ParentElementType
            ,ParentElementCode AS ParentElementCode
            ${overviewSectionPlanComposer.identificationLabels.labelColumnNamesFragment()}
            ,mConceptTranslation.Role AS TranslationRole
            ,mLanguage.IsoCode AS TranslationLanguage
            ,mConceptTranslation.Text AS Translation

            FROM
            ${overviewSectionPlanComposer.elementOverviewQueryName(dpmElementQueryQueryDescriptor)}

            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = ElementConceptId
            LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID
            )
            """.trimLineStartsAndConsequentBlankLines()
    }

    private fun elementTranslationQueryName(
        dpmElementQueryQueryDescriptor: DpmElementQueryDescriptor
    ): String {
        return "${dpmElementQueryQueryDescriptor.elementType}Translation"
    }
}
