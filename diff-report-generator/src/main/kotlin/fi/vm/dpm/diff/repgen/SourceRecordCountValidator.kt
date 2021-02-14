package fi.vm.dpm.diff.repgen

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.model.thisShouldNeverHappen
import fi.vm.dpm.diff.repgen.dpm.utils.SourceTableDescriptor

object SourceRecordCountValidator {

    fun validateCountWithSourceTableTotalRows(
        sourceRecordCount: Int,
        sourceTableDescriptors: List<Any>,
        dbConnection: DbConnection,
        sectionTitle: String,
        diagnostic: Diagnostic
    ) {
        val validationTitle = "SourceRecord count validation (${dbConnection.sourceKind}):"

        val totalRowCount = dbConnection.executeQuery(
            query = sourceTableTotalRowCountQuery(sourceTableDescriptors),
            queryDebugName = "$sectionTitle TotalRowCount"
        ) { resultSet ->
            resultSet.next()
            resultSet.getInt("TotalRowCount")
        }

        if (sourceRecordCount != totalRowCount) {
            diagnostic.fatal(
                """
                $validationTitle Fail
                - Count mismatch between loaded source records and source table rows
                - Section: $sectionTitle
                - Database: ${dbConnection.dbPath}
                - Loaded SourceRecords count: $sourceRecordCount
                - SourceTable(s) total row count: $totalRowCount
                """.trimLineStartsAndConsequentBlankLines()
            )
        } else {
            diagnostic.debug("$validationTitle OK")
        }
    }

    private fun sourceTableTotalRowCountQuery(
        sourceTableDescriptors: List<Any>
    ): String {

        val sourceTableTotalRowCountQuery =
            """
            SELECT
            SUM (TableRowCount) As TotalRowCount

            FROM (
            ${sourceTableRowCountQueries(sourceTableDescriptors).joinToString(separator = "\nUNION ALL\n")}
            )
            """.trimLineStartsAndConsequentBlankLines()

        return sourceTableTotalRowCountQuery
    }

    private fun sourceTableRowCountQueries(
        sourceTableDescriptors: List<Any>
    ): List<String> {

        return sourceTableDescriptors.map { descriptor ->

            val sb = StringBuilder()
            sb.append("SELECT COUNT(*) AS TableRowCount")

            when (descriptor) {

                is String -> {
                    sb.append("\nFROM $descriptor")
                }

                is SourceTableDescriptor -> {
                    sb.append("\nFROM ${descriptor.table}")

                    if (descriptor.joins != null) {
                        descriptor.joins.forEach { join ->
                            sb.append("\nLEFT JOIN $join")
                        }
                    }

                    if (descriptor.where != null) {
                        sb.append("\nWHERE ${descriptor.where}")
                    }
                }

                else -> thisShouldNeverHappen("Unsupported SourceTableDescriptor type: ${descriptor.javaClass.simpleName}")
            }

            sb.toString()
        }
    }
}
