package fi.vm.dpm.diff.model

data class ChangeReport(
    val reportKind: ChangeReportKind,
    val createdAt: String,
    val baselineFileName: String,
    val currentFileName: String,
    val sections: List<ReportSection>,
    val reportGeneratorDescriptor: ReportGeneratorDescriptor,
    val reportGenerationOptions: List<String>
)
