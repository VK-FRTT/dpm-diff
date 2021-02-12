package fi.vm.dpm.diff.model

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.diagnostic.DiagnosticCollector
import fi.vm.dpm.diff.model.diagnostic.ValidationResults
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SectionOutlineTest {

    private val keyField = KeyField(
        fieldName = "keyField",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private val unboundKeyField = KeyField(
        fieldName = "unboundKeyField",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private val recordIdentityFallbackField = RecordIdentityFallbackField(
        identityFallbacks = emptyList()
    )

    private val changeKindField = ChangeKindField()

    private val noteField = NoteField()

    private lateinit var validationResults: ValidationResults
    private lateinit var diagnosticCollector: DiagnosticCollector

    @BeforeEach
    fun testBaseInit() {
        validationResults = ValidationResults()
        diagnosticCollector = DiagnosticCollector()
    }

    @Test
    fun `validation should fail when sectionShortTitle is empty`() {
        createSectionOutline(
            sectionShortTitle = ""
        ).validate(validationResults)

        reportValidationErrors()

        Assertions.assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionOutline.sectionShortTitle: is blank"
        )
    }

    @Test
    fun `validation should fail when sectionTitle is empty`() {
        createSectionOutline(
            sectionTitle = ""
        ).validate(validationResults)

        reportValidationErrors()

        Assertions.assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionOutline.sectionTitle: is blank"
        )
    }

    @Test
    fun `validation should fail when sectionDescription is empty`() {
        createSectionOutline(
            sectionDescription = ""
        ).validate(validationResults)

        reportValidationErrors()

        Assertions.assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionOutline.sectionDescription: is blank"
        )
    }

    @Test
    fun `validation should fail when sectionFields is empty`() {
        createSectionOutline(
            sectionFields = emptyList(),
            sectionSortOrder = emptyList()
        ).validate(validationResults)

        reportValidationErrors()

        Assertions.assertThat(diagnosticCollector.messages).contains(
            """
            FATAL: - SectionOutline.sectionFields: must have one RecordIdentityFallbackField
            - SectionOutline.sectionFields: must have one or more KeyField
            - SectionOutline.sectionFields: must have one ChangeKindField
            - SectionOutline.sectionFields: must have one NoteField
            - SectionOutline.sectionFields: must have one or more PRIME_KEY when ChangeDetectionMode is CORRELATE_BY_KEY_FIELDS
            - SectionOutline.sectionSortOrder: is empty
            """.trimLineStartsAndConsequentBlankLines()
        )
    }

    @Test
    fun `validation should fail when sectionSortOrder is empty`() {
        createSectionOutline(
            sectionSortOrder = emptyList()
        ).validate(validationResults)

        reportValidationErrors()

        Assertions.assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionOutline.sectionSortOrder: is empty"
        )
    }

    @Test
    fun `validation should fail when includedChanges is empty`() {
        createSectionOutline(
            includedChanges = emptySet()
        ).validate(validationResults)

        reportValidationErrors()

        Assertions.assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionOutline.includedChanges: is empty"
        )
    }

    private fun createSectionOutline(
        sectionShortTitle: String = "SECTION-SHORT-TITLE",
        sectionTitle: String = "SECTION-TITLE",
        sectionDescription: String = "SECTION-DESCRIPTION",
        sectionChangeDetectionMode: ChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
        sectionFields: List<Field> = listOf(
            keyField,
            recordIdentityFallbackField,
            changeKindField,
            noteField
        ),
        sectionSortOrder: List<SortBy> = listOf(
            NumberAwareSortBy(keyField)
        ),
        includedChanges: Set<ChangeKind> = ChangeKind.allChanges()

    ): SectionOutline {
        return SectionOutline(
            sectionShortTitle = sectionShortTitle,
            sectionTitle = sectionTitle,
            sectionDescription = sectionDescription,
            sectionChangeDetectionMode = sectionChangeDetectionMode,
            sectionFields = sectionFields,
            sectionSortOrder = sectionSortOrder,
            includedChanges = includedChanges
        )
    }

    private fun reportValidationErrors() {
        val thrown = Assertions.catchThrowable {
            validationResults.reportErrors(diagnosticCollector)
        }

        assertThat(thrown).isInstanceOf(ArithmeticException::class.java)
    }
}
