package fi.vm.dpm.diff.repgen.section.reportingframework

import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.ElementTranslationHelpers.elementTranslationSourceTableDescriptor
import fi.vm.dpm.diff.repgen.section.ElementTranslationSectionBase

class ReportingFrameworkTranslationSection(
    generationContext: GenerationContext,
    translationLangCodes: List<String>?
) : ElementTranslationSectionBase(
    generationContext
) {
    override val sectionDescriptor = elementTranslationSectionDescriptor(
        sectionShortTitle = "RepFwTranslation",
        sectionTitle = "ReportingFramework translations",
        sectionDescription = "Label and description changes in Reporting Frameworks, Taxonomies, Modules and Tables"
    )

    override val queryColumnMapping = elementTranslationQueryColumnMappings()

    override val query = elementTranslationQuery(
        ReportingFrameworkOverviewSection.elementQueryDescriptors,
        translationLangCodes
    )

    override val sourceTableDescriptors = listOf(
        elementTranslationSourceTableDescriptor(
            elementTable = "mReportingFramework",
            conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mReportingFramework.ConceptID",
            translationLangCodes = translationLangCodes
        ),

        elementTranslationSourceTableDescriptor(
            elementTable = "mTaxonomy",
            conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mTaxonomy.ConceptID",
            translationLangCodes = translationLangCodes
        ),

        elementTranslationSourceTableDescriptor(
            elementTable = "mModule",
            conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mModule.ConceptID",
            translationLangCodes = translationLangCodes
        ),

        elementTranslationSourceTableDescriptor(
            elementTable = "mTable",
            conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mTable.ConceptID",
            translationLangCodes = translationLangCodes
        )
    )

    init {
        sanityCheckSectionConfig()
    }
}
