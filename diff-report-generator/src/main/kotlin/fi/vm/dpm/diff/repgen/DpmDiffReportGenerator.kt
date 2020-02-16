package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.DbConnection
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.DictionaryElementOverviewSection
import fi.vm.dpm.diff.repgen.section.DictionaryTranslationsSection
import fi.vm.dpm.diff.repgen.section.DimensionSection
import fi.vm.dpm.diff.repgen.section.DomainSection
import fi.vm.dpm.diff.repgen.section.HierarchyNodeSection
import fi.vm.dpm.diff.repgen.section.HierarchyNodeStructureSection
import fi.vm.dpm.diff.repgen.section.HierarchySection
import fi.vm.dpm.diff.repgen.section.MemberSection
import fi.vm.dpm.diff.repgen.section.MetricSection
import java.io.Closeable
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    fun generateReport(): ChangeReport {
        with(diagnostic) {
            info("Finding changes...")
        }

        val generationContext = GenerationContext(
            baselineConnection = baselineConnection,
            actualConnection = actualConnection,
            identificationLabelLangCodes = listOf("fi", "sv"), // TODO
            diagnostic = diagnostic
        )

        val sections = listOf(
            DictionaryElementOverviewSection(generationContext),
            DictionaryTranslationsSection(generationContext),
            DomainSection(generationContext),
            MemberSection(generationContext),
            MetricSection(generationContext),
            DimensionSection(generationContext),
            HierarchySection(generationContext),
            HierarchyNodeSection(generationContext),
            HierarchyNodeStructureSection(generationContext)
        )

        val generatedSections = sections.map { it.generateSection() }

        return ChangeReport(
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")),
            baselineDpmDbFileName = baselineDpmDbPath.fileName.toString(),
            actualDpmDbFileName = actualDpmDbPath.fileName.toString(),
            sections = generatedSections
        )
    }
}
