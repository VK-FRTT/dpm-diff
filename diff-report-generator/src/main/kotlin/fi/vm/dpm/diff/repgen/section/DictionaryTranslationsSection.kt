package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.AtomOption
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.CorrelationKeyField
import fi.vm.dpm.diff.model.CorrelationKeyKind
import fi.vm.dpm.diff.model.CorrelationMode
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.FixedDictionaryElementTypeSort
import fi.vm.dpm.diff.model.FixedTranslationRoleSort
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SourceTableDescriptor

class DictionaryTranslationsSection(
    generationContext: GenerationContext
) : DictionarySectionBase(
    generationContext
) {
    private val translationRole = CorrelationKeyField(
        fieldName = "TranslationRole",
        correlationKeyKind = CorrelationKeyKind.SECONDARY_KEY,
        correlationFallback = null,
        noteFallbacks = emptyList()
    )

    private val translationLanguage = CorrelationKeyField(
        fieldName = "Language",
        correlationKeyKind = CorrelationKeyKind.SECONDARY_KEY,
        correlationFallback = null,
        noteFallbacks = emptyList()
    )

    private val translation = AtomField(
        fieldName = "Translation",
        atomOptions = AtomOption.OUTPUT_TO_ADDED_CHANGE
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "DictTranslations",
        sectionTitle = "Dictionary Translations",
        sectionDescription = "Dictionary: Label and Description changes",
        sectionFields = listOf(
            elementId,
            elementInherentLabel,
            parentElementType,
            parentElementCode,
            elementType,
            elementCode,
            *identificationLabels,
            translationRole,
            translationLanguage,
            changeKind,
            translation,
            note
        ),
        sectionSortOrder = listOf(
            FixedDictionaryElementTypeSort(parentElementType),
            NumberAwareSort(parentElementCode),
            FixedDictionaryElementTypeSort(elementType),
            NumberAwareSort(elementCode),
            FixedTranslationRoleSort(translationRole),
            NumberAwareSort(translationLanguage),
            FixedChangeKindSort(changeKind)
        ),
        correlationMode = CorrelationMode.TWO_PHASE_BY_PRIMARY_AND_FULL_KEY,
        includedChanges = ChangeKind.allValues()

    )

    override val queryColumnMapping = mapOf(
        "ElementId" to elementId,
        "ElementInherentLabel" to elementInherentLabel,
        "ElementType" to elementType,
        "ElementCode" to elementCode,
        "ParentElementType" to parentElementType,
        "ParentElementCode" to parentElementCode,
        *idLabelColumnMapping(),
        "TranslationRole" to translationRole,
        "TranslationLanguage" to translationLanguage,
        "Translation" to translation
    )

    override val query = run {

        val query =
            """
            -- Shared sub-queries
            WITH ${elementQueryDescriptors
                .map {
                    """
                    ${elementEssentialsQueryExpression(it)},
                    ${elementTranslationsQueryExpression(it)}
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
            ${idLabelColumnNamesFragment()}
            ,TranslationRole AS TranslationRole
            ,TranslationLanguage AS TranslationLanguage
            ,Translation AS Translation

            FROM (
                ${elementQueryDescriptors
                .map { "SELECT * FROM ${elementTranslationsQueryName(it)}" }
                .joinToString("\nUNION ALL\n")}
            )

            ORDER BY ElementType, ParentElementCode, ElementCode
            """

        query.trimLineStartsAndConsequentBlankLines()
    }

    private fun elementTranslationsQueryExpression(
        elementQueryDescription: ElementQueryDescriptor
    ): String {

        val essentialsQueryName = elementEssentialsQueryName(elementQueryDescription)
        val translationsQueryName = elementTranslationsQueryName(elementQueryDescription)

        return """
            $translationsQueryName AS (

            SELECT
            ElementType AS ElementType
            ,ElementId AS ElementId
            ,ElementInherentLabel AS ElementInherentLabel
            ,ElementCode AS ElementCode
            ,ParentElementType AS ParentElementType
            ,ParentElementCode AS ParentElementCode
            ${idLabelColumnNamesFragment()}
            ,mConceptTranslation.Role AS TranslationRole
            ,mLanguage.IsoCode AS TranslationLanguage
            ,mConceptTranslation.Text AS Translation

            FROM
            $essentialsQueryName

            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = ElementConceptId
            LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID
            )
            """.trimLineStartsAndConsequentBlankLines()
    }

    private fun elementTranslationsQueryName(
        elementQueryDescription: ElementQueryDescriptor
    ): String {
        return "${elementQueryDescription.elementType}Translations"
    }

    override val sourceTableDescriptors = listOf(
        SourceTableDescriptor(
            table = "mDomain",
            join = "mConceptTranslation on mConceptTranslation.ConceptID = mDomain.ConceptID"
        ),
        SourceTableDescriptor(
            table = "mMember",
            join = "mConceptTranslation on mConceptTranslation.ConceptID = mMember.ConceptID"
        ),
        SourceTableDescriptor(
            table = "mDimension",
            join = "mConceptTranslation on mConceptTranslation.ConceptID = mDimension.ConceptID"
        ),
        SourceTableDescriptor(
            table = "mHierarchy",
            join = "mConceptTranslation on mConceptTranslation.ConceptID = mHierarchy.ConceptID"
        )
    )

    init {
        sanityCheckSectionConfig()
    }
}
