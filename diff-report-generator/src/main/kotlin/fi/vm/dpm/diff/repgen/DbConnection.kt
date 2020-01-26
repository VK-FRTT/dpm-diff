package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import java.io.Closeable
import java.nio.file.Path
import java.sql.DriverManager
import java.sql.ResultSet

class DbConnection(
    val dbPath: Path,
    private val diagnostic: Diagnostic
) : Closeable {

    private val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

    override fun close() {
        connection.close()
    }

    fun <R> executeQuery(
        query: String,
        action: (ResultSet) -> R
    ): R {
        diagnostic.debug("DB query:[\n${query.prependIndent()}\n]")

        return connection.createStatement().use { statement ->
            val rs = statement.executeQuery(query)
            action(rs)
        }
    }
}
