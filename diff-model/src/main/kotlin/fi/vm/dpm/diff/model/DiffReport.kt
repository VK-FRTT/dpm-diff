package fi.vm.dpm.diff.model

data class DiffReport(
    val createdAt: String,
    val baselineDpmDbFileName: String,
    val actualDpmDbFileName: String,
    val sections: List<ReportSection>
)
