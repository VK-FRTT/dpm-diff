package fi.vm.dpm.diff.model

data class ReportSection(
    val sectionOutline: SectionOutline,
    val baselineSourceRecords: Int,
    val currentSourceRecords: Int,
    val changes: List<ChangeRecord>
)
