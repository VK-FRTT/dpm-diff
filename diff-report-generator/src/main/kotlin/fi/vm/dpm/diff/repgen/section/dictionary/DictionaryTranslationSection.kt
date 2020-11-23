package fi.vm.dpm.diff.repgen.section.dictionary

import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.ElementTranslationHelpers.elementTranslationSourceTableDescriptor
import fi.vm.dpm.diff.repgen.section.ElementTranslationSectionBase

class DictionaryTranslationSection(
    generationContext: GenerationContext,
    translationLangCodes: List<String>?
) : ElementTranslationSectionBase(
    generationContext
) {
    override val sectionDescriptor = elementTranslationSectionDescriptor(
        sectionShortTitle = "DictTranslation",
        sectionTitle = "Dictionary translations",
        sectionDescription = "Label and description changes in Domains, Members, Metrics, Dimensions and Hierarchies"
    )

    override val queryColumnMapping = elementTranslationQueryColumnMappings()

    override val query = elementTranslationQuery(
        DictionaryOverviewSection.elementQueryDescriptors,
        translationLangCodes
    )

    override val sourceTableDescriptors = listOf(
        elementTranslationSourceTableDescriptor(
            elementTable = "mDomain",
            conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mDomain.ConceptID",
            translationLangCodes = translationLangCodes
        ),

        elementTranslationSourceTableDescriptor(
            elementTable = "mMember",
            conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mMember.ConceptID",
            translationLangCodes = translationLangCodes
        ),

        elementTranslationSourceTableDescriptor(
            elementTable = "mDimension",
            conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mDimension.ConceptID",
            translationLangCodes = translationLangCodes
        ),

        elementTranslationSourceTableDescriptor(
            elementTable = "mHierarchy",
            conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mHierarchy.ConceptID",
            translationLangCodes = translationLangCodes
        )
    )

    init {
        sanityCheckSectionConfig()
    }
}
