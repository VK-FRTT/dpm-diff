package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.ReportGenerator
import fi.vm.dpm.diff.repgen.SQLiteDbConnection
import fi.vm.dpm.diff.repgen.dpm.VkDataGenerationContext
import fi.vm.dpm.diff.repgen.dpm.section.SectionBase
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class VkDataReportGenerator(
    private val baselineDbPath: Path,
    private val currentDbPath: Path,
    private val reportGeneratorDescriptor: ReportGeneratorDescriptor,
    private val diagnostic: Diagnostic
) : ReportGenerator {

    private val baselineConnection: SQLiteDbConnection by lazy {
        SQLiteDbConnection(baselineDbPath, diagnostic)
    }

    private val currentConnection: SQLiteDbConnection by lazy {
        SQLiteDbConnection(currentDbPath, diagnostic)
    }

    override fun close() {
        baselineConnection.close()
        currentConnection.close()
    }

    override fun generateReport(): ChangeReport {
        val generationContext = VkDataGenerationContext(
            baselineConnection = baselineConnection,
            currentConnection = currentConnection,
            diagnostic = diagnostic
        )

        val sections = emptyList<SectionBase>()

        val generatedSections = sections.map { it.generateSection() }

        return ChangeReport(
            reportKind = ChangeReportKind.VK_DATA,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")),
            baselineFileName = baselineDbPath.fileName.toString(),
            currentFileName = currentDbPath.fileName.toString(),
            sections = generatedSections,
            reportGeneratorDescriptor = reportGeneratorDescriptor,
            reportGenerationOptions = emptyList()
        )
    }
}
