package fi.vm.dpm.diff.repgen

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import java.sql.ResultSetMetaData

object QueryColumnMappingValidator {

    fun validateColumnNamesMatch(
        queryColumnMapping: Map<String, Field>,
        resultSetMetaData: ResultSetMetaData,
        diagnostic: Diagnostic
    ) {
        val validationTitle = "QueryColumnMapping validation:"

        val resultSetColumnLabels =
            (1..resultSetMetaData.columnCount)
                .map { resultSetMetaData.getColumnLabel(it) }
                .toTypedArray()

        val mappingColumnLabels =
            queryColumnMapping
                .map { (columnLabel, _) -> columnLabel }
                .toTypedArray()

        if (!(resultSetColumnLabels contentDeepEquals mappingColumnLabels)) {
            diagnostic.fatal(
                """
                $validationTitle Fail
                - Column name mismatch between QueryColumnMapping and ResultSetMetaData
                - ResultSetMetaData columns: ${resultSetColumnLabels.joinToString()}
                - QueryColumnMapping columns: ${mappingColumnLabels.joinToString()}
                """.trimLineStartsAndConsequentBlankLines()
            )
        } else {
            diagnostic.debug("$validationTitle OK")
        }
    }
}
