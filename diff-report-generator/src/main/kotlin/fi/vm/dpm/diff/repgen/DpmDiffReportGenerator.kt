package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.DbConnection
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.DimensionSection
import fi.vm.dpm.diff.repgen.section.DomainSection
import fi.vm.dpm.diff.repgen.section.MemberSection
import java.io.Closeable
import java.nio.file.Path
import java.time.LocalDateTime

class DpmDiffReportGenerator(
    private val baselineDpmDbPath: Path,
    private val actualDpmDbPath: Path,
    private val diagnostic: Diagnostic
) : Closeable {

    private val baselineConnection: DbConnection by lazy {
        DbConnection(baselineDpmDbPath, diagnostic)
    }

    private val actualConnection: DbConnection by lazy {
        DbConnection(actualDpmDbPath, diagnostic)
    }

    override fun close() {
        baselineConnection.close()
        actualConnection.close()
    }

    fun generateReport(): DifferenceReport {
        with(diagnostic) {
            info("Finding differences...")
        }

        val generationContext = GenerationContext(
            baselineConnection = baselineConnection,
            actualConnection = actualConnection,
            discriminationLangCodes = listOf("fi", "sv"), // TODO
            diagnostic = diagnostic
        )

        val sections = emptyList<ReportSection>() +
            MemberSection(generationContext).generateSection() +
            DomainSection(generationContext).generateSection() +
            DimensionSection(generationContext).generateSection()

        return DifferenceReport(
            createdAt = LocalDateTime.now().toString(),
            baselineDpmDbFileName = baselineDpmDbPath.fileName.toString(),
            actualDpmDbFileName = actualDpmDbPath.fileName.toString(),
            sections = sections
        )
    }
}
