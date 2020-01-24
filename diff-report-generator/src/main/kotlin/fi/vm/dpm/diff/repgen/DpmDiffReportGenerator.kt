package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.dictionaryElementsSection
import java.io.Closeable
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDateTime

class DpmDiffReportGenerator(
    private val baselineDpmDbPath: Path,
    private val actualDpmDbPath: Path,
    diagnostic: Diagnostic
) : Closeable {

    private val baselineConnection: Connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:$baselineDpmDbPath")
    }

    private val actualConnection: Connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:$actualDpmDbPath")
    }

    override fun close() {
        baselineConnection.close()
        actualConnection.close()
    }

    fun generateReport(): DiffReport {
        val generationContext = GenerationContext(
            baselineConnection = baselineConnection,
            actualConnection = actualConnection
        )

        val sections = emptyList<ReportSection>() +
            dictionaryElementsSection(generationContext)

        return DiffReport(
            createdAt = LocalDateTime.now().toString(),
            baselineDpmDbFileName = baselineDpmDbPath.fileName.toString(),
            actualDpmDbFileName = actualDpmDbPath.fileName.toString(),
            sections = sections
        )
    }
}
