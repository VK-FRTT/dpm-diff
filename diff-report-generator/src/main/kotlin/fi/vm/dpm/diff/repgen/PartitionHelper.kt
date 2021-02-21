package fi.vm.dpm.diff.repgen

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import kotlin.math.ceil

object PartitionHelper {

    fun getPartitionCount(
        sourceDbs: SourceDbs,
        tableName: String
    ): Int {
        val partitionCount = maxOf(
            1,
            getPartitionCount(sourceDbs.baselineConnection, tableName),
            getPartitionCount(sourceDbs.currentConnection, tableName)
        )

        return partitionCount
    }

    private fun getPartitionCount(
        dbConnection: DbConnection,
        tableName: String
    ): Int {
        val rowCount = getRowCount(dbConnection, tableName).toFloat()
        return ceil(rowCount / MAX_ITEMS_PER_PARTITION).toInt()
    }

    private fun getRowCount(
        dbConnection: DbConnection,
        tableName: String
    ): Int {
        val totalRowCountQuery = """
            SELECT COUNT(*) As RowCount
            FROM $tableName
        """.trimLineStartsAndConsequentBlankLines()

        val queryDebugName = "$tableName RowCount"

        val count = dbConnection.executeQuery(totalRowCountQuery, queryDebugName) { resultSet ->
            resultSet.next()
            resultSet.getInt("RowCount")
        }

        return count
    }
}
