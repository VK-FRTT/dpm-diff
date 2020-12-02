package fi.vm.dpm.diff.model

data class ReportSection(
    val sectionOutline: SectionOutline,
    val changes: List<ChangeRecord>
)
