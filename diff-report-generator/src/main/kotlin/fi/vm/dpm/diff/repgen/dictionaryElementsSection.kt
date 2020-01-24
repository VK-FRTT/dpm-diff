package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.ReportSection
import fi.vm.dpm.diff.model.SectionDescriptor

fun dictionaryElementsSection(
    generationContext: GenerationContext
): ReportSection {

    /*
    val sectionColumns = listOf(
        Column(
            title = "dictionary element code"
        ),
        Column(
            title = "dictionary element type"
        )
        ,
        Column(
            title = "dictionary element label fi"
        )
        ,
        Column(
            title = "dictionary element label sv"
        )
        ,
        Column(
            title = "change"
        )
    )
    */

    val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Dict Elements",
        sectionTitle = "Dictionary elements",
        sectionDescription = "Dictionary elements: added and removed Members, Metrics, Domains, Dimensions, Hierarchies and HierarchyNodes"
    )

    return ReportSection(
        sectionDescriptor = sectionDescriptor
    )
}
