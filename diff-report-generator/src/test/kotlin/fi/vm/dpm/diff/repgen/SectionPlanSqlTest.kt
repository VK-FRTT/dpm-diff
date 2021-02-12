package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.DiagnosticCollector
import fi.vm.dpm.diff.model.diagnostic.ValidationResults
import fi.vm.dpm.diff.repgen.SectionPlanSql
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SectionPlanSqlTest {

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

    private val sectionOutline = SectionOutline(
        sectionShortTitle = "SECTION-SHORT-TITLE",
        sectionTitle = "SECTION-TITLE",
        sectionDescription = "SECTION-DESCRIPTION",
        sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
        sectionFields = listOf(
            keyField,
            recordIdentityFallbackField,
            changeKindField,
            noteField
        ),
        sectionSortOrder = listOf(
            NumberAwareSortBy(keyField)
        ),

        includedChanges = ChangeKind.allChanges()
    )

    private lateinit var validationResults: ValidationResults
    private lateinit var diagnosticCollector: DiagnosticCollector

    @BeforeEach
    fun testBaseInit() {
        validationResults = ValidationResults()
        diagnosticCollector = DiagnosticCollector()
    }

    @Test
    fun `validation should fail when queryColumnMapping is empty`() {
        createSectionPlanSqlWithSingleQuery(
            queryColumnMapping = emptyMap()
        ).validate(validationResults)

        reportValidationErrors()

        assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionPlanSql.queryColumnMapping: is empty"
        )
    }

    @Test
    fun `validation should fail when queryColumnMapping refers unknown Field`() {
        createSectionPlanSqlWithSingleQuery(
            queryColumnMapping = mapOf(
                "DbColumnName" to unboundKeyField
            )
        ).validate(validationResults)

        reportValidationErrors()

        assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionPlanSql.queryColumnMapping: has unknown field (unboundKeyField)"
        )
    }

    @Test
    fun `validation should fail when queryColumnMapping has multiple entries for one Field`() {
        createSectionPlanSqlWithSingleQuery(
            queryColumnMapping = mapOf(
                "DbColumnName1" to keyField,
                "DbColumnName2" to keyField
            )
        )
            .validate(validationResults)

        reportValidationErrors()

        assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionPlanSql.queryColumnMapping: has duplicate field (keyField)"
        )
    }

    @Test
    fun `validation should fail when query is empty`() {
        SectionPlanSql
            .withPartitionedQueries(
                sectionOutline = sectionOutline,
                queryColumnMapping = mapOf("DbColumnName" to keyField),
                partitionedQueries = emptyList(),
                sourceTableDescriptors = listOf("DbTableName")
            )
            .validate(validationResults)

        reportValidationErrors()

        assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionPlanSql.partitionedQueries: is empty"
        )
    }

    @Test
    fun `validation should fail when sourceTableDescriptors is empty`() {
        createSectionPlanSqlWithSingleQuery(
            sourceTableDescriptors = emptyList()
        ).validate(validationResults)

        reportValidationErrors()

        assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionPlanSql.sourceTableDescriptors: is empty"
        )
    }

    @Test
    fun `validation should fail when sourceTableDescriptors has unsupported type`() {
        createSectionPlanSqlWithSingleQuery(
            sourceTableDescriptors = listOf("string", 1)
        ).validate(validationResults)

        reportValidationErrors()

        assertThat(diagnosticCollector.messages).contains(
            "FATAL: - SectionPlanSql.sourceTableDescriptors: has unsupported descriptor type"
        )
    }

    private fun createSectionPlanSqlWithSingleQuery(
        queryColumnMapping: Map<String, Field> = mapOf(
            "DbColumnName" to keyField
        ),
        query: String = "SqlQuery",
        sourceTableDescriptors: List<Any> = listOf("DbTableName")

    ): SectionPlanSql {
        return SectionPlanSql
            .withSingleQuery(
                sectionOutline = sectionOutline,
                queryColumnMapping = queryColumnMapping,
                query = query,
                sourceTableDescriptors = sourceTableDescriptors
            )
    }

    private fun reportValidationErrors() {
        val thrown = catchThrowable {
            validationResults.reportErrors(diagnosticCollector)
        }

        assertThat(thrown).isInstanceOf(ArithmeticException::class.java)
    }
}
