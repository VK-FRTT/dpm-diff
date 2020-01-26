package fi.vm.dpm.diff.model

data class ReportSection(
    val sectionDescriptor: SectionDescriptor,
    val differences: List<DifferenceRecord>
)
