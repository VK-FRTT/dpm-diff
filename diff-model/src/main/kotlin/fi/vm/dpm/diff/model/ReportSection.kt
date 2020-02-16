package fi.vm.dpm.diff.model

data class ReportSection(
    val sectionDescriptor: SectionDescriptor,
    val changes: List<ChangeRecord>
)
