package fi.vm.dpm.diff.repgen

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.diagnostic.DiagnosticCollector
import java.sql.ResultSetMetaData
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class QueryColumnMappingValidatorTest {

    private val keyField = KeyField(
        fieldName = "keyField",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private lateinit var diagnosticCollector: DiagnosticCollector

    @BeforeEach
    fun testBaseInit() {
        diagnosticCollector = DiagnosticCollector()
    }

    @Test
    fun `Should emit diagnostic message when column names in queryColumnMapping and ResultSetMetaData do not match`() {

        val queryColumnMapping = mapOf(
            "mappingColumnName" to keyField
        )

        val resultSetMetaData = mock<ResultSetMetaData> {
            on { columnCount }.doReturn(1)
            on { getColumnLabel(1) }.doReturn("dbColumnName")
        }

        val thrown = Assertions.catchThrowable {
            QueryColumnMappingValidator.validateColumnNamesMatch(
                queryColumnMapping,
                resultSetMetaData,
                diagnosticCollector
            )
        }

        assertThat(diagnosticCollector.messages).contains(
            """
            FATAL: QueryColumnMapping validation: Fail
            - Column name mismatch between QueryColumnMapping and ResultSetMetaData
            - ResultSetMetaData columns: dbColumnName
            - QueryColumnMapping columns: mappingColumnName
            """.trimLineStartsAndConsequentBlankLines()
        )

        assertThat(thrown).isNotNull()
    }

    @Test
    fun `Should emit diagnostic success messages when column names match`() {

        val queryColumnMapping = mapOf(
            "columnName" to keyField
        )

        val resultSetMetaData = mock<ResultSetMetaData> {
            on { columnCount }.doReturn(1)
            on { getColumnLabel(1) }.doReturn("columnName")
        }

        val thrown = Assertions.catchThrowable {
            QueryColumnMappingValidator.validateColumnNamesMatch(
                queryColumnMapping,
                resultSetMetaData,
                diagnosticCollector
            )
        }

        assertThat(diagnosticCollector.messages).contains("DEBUG: QueryColumnMapping validation: OK")
        assertThat(thrown).isNull()
    }
}
