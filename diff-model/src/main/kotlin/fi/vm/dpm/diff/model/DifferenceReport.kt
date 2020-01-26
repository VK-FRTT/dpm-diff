package fi.vm.dpm.diff.model

data class DifferenceReport(
    val createdAt: String,
    val baselineDpmDbFileName: String,
    val actualDpmDbFileName: String,
    val sections: List<ReportSection>
)
