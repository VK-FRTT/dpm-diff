package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.model.SourceKind
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import java.io.Closeable
import java.nio.file.Path
import java.sql.DriverManager
import java.sql.ResultSet
import org.sqlite.SQLiteException

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
        queryName: String,
        action: (ResultSet) -> R
    ): R {
        diagnostic.debug("DB query '$queryName':[\n${query.prependIndent()}\n]")

        return connection.createStatement().use { statement ->
            val rs = try {
                statement.executeQuery(query)
            } catch (exception: SQLiteException) {
                val messageTitle = "DB query failure:"

                diagnostic.debug("$messageTitle\n${exception.stackTrace.joinToString(separator = "\n")}")
                diagnostic.fatal("$messageTitle ${exception.message}")
            }

            action(rs)
        }
    }
}
