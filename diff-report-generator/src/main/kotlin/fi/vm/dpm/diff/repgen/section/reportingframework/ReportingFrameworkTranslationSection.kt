package fi.vm.dpm.diff.repgen.section.reportingframework

import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SourceTableDescriptor
import fi.vm.dpm.diff.repgen.section.ElementTranslationSectionBase

class ReportingFrameworkTranslationSection(
    generationContext: GenerationContext
) : ElementTranslationSectionBase(
    generationContext
) {
    override val sectionDescriptor = elementTranslationSectionDescriptor(
        sectionShortTitle = "RepFwTranslation",
        sectionTitle = "ReportingFramework Translations",
        sectionDescription = "ReportingFramework: Label and Description changes"
    )

    override val queryColumnMapping = elementTranslationQueryColumnMappings()

    override val query = elementTranslationQuery(ReportingFrameworkOverviewSection.elementQueryDescriptors)

    override val sourceTableDescriptors = listOf(
        SourceTableDescriptor(
            table = "mReportingFramework",
            join = "mConceptTranslation on mConceptTranslation.ConceptID = mReportingFramework.ConceptID"
        ),
        SourceTableDescriptor(
            table = "mTaxonomy",
            join = "mConceptTranslation on mConceptTranslation.ConceptID = mTaxonomy.ConceptID"
        ),
        SourceTableDescriptor(
            table = "mModule",
            join = "mConceptTranslation on mConceptTranslation.ConceptID = mModule.ConceptID"
        ),
        SourceTableDescriptor(
            table = "mTable",
            join = "mConceptTranslation on mConceptTranslation.ConceptID = mTable.ConceptID"
        )
    )

    init {
        sanityCheckSectionConfig()
    }
}
