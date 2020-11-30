package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.ReportGenerator
import fi.vm.dpm.diff.repgen.SQLiteDbConnection
import fi.vm.dpm.diff.repgen.dpm.DpmGenerationContext
import fi.vm.dpm.diff.repgen.dpm.section.dictionary.DictionaryOverviewSection
import fi.vm.dpm.diff.repgen.dpm.section.dictionary.DictionaryTranslationSection
import fi.vm.dpm.diff.repgen.dpm.section.dictionary.DimensionSection
import fi.vm.dpm.diff.repgen.dpm.section.dictionary.DomainSection
import fi.vm.dpm.diff.repgen.dpm.section.dictionary.HierarchyNodeSection
import fi.vm.dpm.diff.repgen.dpm.section.dictionary.HierarchyNodeStructureSection
import fi.vm.dpm.diff.repgen.dpm.section.dictionary.HierarchySection
import fi.vm.dpm.diff.repgen.dpm.section.dictionary.MemberSection
import fi.vm.dpm.diff.repgen.dpm.section.dictionary.MetricSection
import fi.vm.dpm.diff.repgen.dpm.section.reportingframework.AxisOrdinateSection
import fi.vm.dpm.diff.repgen.dpm.section.reportingframework.AxisOrdinateTranslationSection
import fi.vm.dpm.diff.repgen.dpm.section.reportingframework.OrdinateCategorisationSection
import fi.vm.dpm.diff.repgen.dpm.section.reportingframework.ReportingFrameworkOverviewSection
import fi.vm.dpm.diff.repgen.dpm.section.reportingframework.ReportingFrameworkTranslationSection
import fi.vm.dpm.diff.repgen.dpm.section.reportingframework.TableAxisSection
import fi.vm.dpm.diff.repgen.dpm.section.reportingframework.TableSection
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DpmReportGenerator(
    private val baselineDbPath: Path,
    private val currentDbPath: Path,
    private val reportGeneratorDescriptor: ReportGeneratorDescriptor,
    private val identificationLabelLangCodes: List<String>,
    private val translationLangCodes: List<String>?,
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
        val generationContext = DpmGenerationContext(
            baselineConnection = baselineConnection,
            currentConnection = currentConnection,
            identificationLabelLangCodes = identificationLabelLangCodes,
            diagnostic = diagnostic
        )

        val dictionarySections = listOf(
            DictionaryOverviewSection(generationContext),
            DictionaryTranslationSection(generationContext, translationLangCodes),
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
            ReportingFrameworkTranslationSection(generationContext, translationLangCodes),
            TableSection(generationContext),
            TableAxisSection(generationContext),
            AxisOrdinateSection(generationContext),
            AxisOrdinateTranslationSection(generationContext, translationLangCodes),
            OrdinateCategorisationSection(generationContext)
        )

        val generatedSections = (dictionarySections + reportingFrameworkSections).map { it.generateSection() }

        return ChangeReport(
            reportKind = ChangeReportKind.DPM,
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")),
            baselineFileName = baselineDbPath.fileName.toString(),
            currentFileName = currentDbPath.fileName.toString(),
            sections = generatedSections,
            reportGeneratorDescriptor = reportGeneratorDescriptor,
            reportGenerationOptions = listOf(
                "IdentificationLabelLanguages: ${identificationLabelLangCodes.joinToString()}",
                "TranslationLanguages: ${translationLangCodes?.joinToString() ?: "ALL"}"
            )
        )
    }
}
