package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.DbConnection
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.dictionary.DictionaryOverviewSection
import fi.vm.dpm.diff.repgen.section.dictionary.DictionaryTranslationSection
import fi.vm.dpm.diff.repgen.section.dictionary.DimensionSection
import fi.vm.dpm.diff.repgen.section.dictionary.DomainSection
import fi.vm.dpm.diff.repgen.section.dictionary.HierarchyNodeSection
import fi.vm.dpm.diff.repgen.section.dictionary.HierarchyNodeStructureSection
import fi.vm.dpm.diff.repgen.section.dictionary.HierarchySection
import fi.vm.dpm.diff.repgen.section.dictionary.MemberSection
import fi.vm.dpm.diff.repgen.section.dictionary.MetricSection
import fi.vm.dpm.diff.repgen.section.reportingframework.AxisOrdinateSection
import fi.vm.dpm.diff.repgen.section.reportingframework.ReportingFrameworkOverviewSection
import fi.vm.dpm.diff.repgen.section.reportingframework.ReportingFrameworkTranslationSection
import fi.vm.dpm.diff.repgen.section.reportingframework.TableAxisSection
import fi.vm.dpm.diff.repgen.section.reportingframework.TableSection
import java.io.Closeable
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DpmDiffReportGenerator(
    private val baselineDpmDbPath: Path,
    private val currentDpmDbPath: Path,
    private val reportGeneratorDescriptor: ReportGeneratorDescriptor,
    private val identificationLabelLangCodes: List<String>,
    private val diagnostic: Diagnostic
) : Closeable {

    private val baselineConnection: DbConnection by lazy {
        DbConnection(baselineDpmDbPath, diagnostic)
    }

    private val currentConnection: DbConnection by lazy {
        DbConnection(currentDpmDbPath, diagnostic)
    }

    override fun close() {
        baselineConnection.close()
        currentConnection.close()
    }

    fun generateReport(): ChangeReport {
        with(diagnostic) {
            info("Finding changes...")
        }

        val generationContext = GenerationContext(
            baselineConnection = baselineConnection,
            currentConnection = currentConnection,
            identificationLabelLangCodes = identificationLabelLangCodes,
            diagnostic = diagnostic
        )

        val dictionarySections = listOf(
            DictionaryOverviewSection(generationContext),
            DictionaryTranslationSection(generationContext),
            DomainSection(generationContext),
            MemberSection(generationContext),
            MetricSection(generationContext),
            DimensionSection(generationContext),
            HierarchySection(generationContext),
            HierarchyNodeSection(generationContext),
            HierarchyNodeStructureSection(generationContext)
        )

        val reportingFrameworkSections = listOf(
            ReportingFrameworkOverviewSection(generationContext),
            ReportingFrameworkTranslationSection(generationContext),
            TableSection(generationContext),
            TableAxisSection(generationContext),
            AxisOrdinateSection(generationContext)
        )

        val generatedSections = (dictionarySections + reportingFrameworkSections).map { it.generateSection() }

        return ChangeReport(
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")),
            baselineDpmDbFileName = baselineDpmDbPath.fileName.toString(),
            currentDpmDbFileName = currentDpmDbPath.fileName.toString(),
            sections = generatedSections,
            reportGeneratorDescriptor = reportGeneratorDescriptor
        )
    }
}
