package fi.vm.dpm.diff.model

data class ChangeReport(
    val createdAt: String,
    val baselineDpmDbFileName: String,
    val currentDpmDbFileName: String,
    val sections: List<ReportSection>,
    val reportGeneratorDescriptor: ReportGeneratorDescriptor
)
