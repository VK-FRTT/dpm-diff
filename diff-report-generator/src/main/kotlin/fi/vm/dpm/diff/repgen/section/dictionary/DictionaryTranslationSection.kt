package fi.vm.dpm.diff.repgen.section.dictionary

import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SourceTableDescriptor
import fi.vm.dpm.diff.repgen.section.ElementTranslationSectionBase

class DictionaryTranslationSection(
    generationContext: GenerationContext
) : ElementTranslationSectionBase(
    generationContext
) {
    override val sectionDescriptor = elementTranslationSectionDescriptor(
        sectionShortTitle = "DictTranslation",
        sectionTitle = "Dictionary translations",
        sectionDescription = "Label and description changes in Domains, Members, Metrics, Dimensions and Hierarchies"
    )

    override val queryColumnMapping = elementTranslationQueryColumnMappings()

    override val query = elementTranslationQuery(DictionaryOverviewSection.elementQueryDescriptors)

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
