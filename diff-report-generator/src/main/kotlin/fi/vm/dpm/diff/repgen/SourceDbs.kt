package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.SourceKind
import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import java.io.Closeable
import java.nio.file.Path

class SourceDbs(
    val baselineDbPath: Path,
    val currentDbPath: Path,
    val jdbcDriver: String,
    val diagnostic: Diagnostic
) : Closeable {

    val baselineConnection: DbConnection by lazy {
        DbConnection(baselineDbPath, jdbcDriver, SourceKind.BASELINE, diagnostic)
    }

    val currentConnection: DbConnection by lazy {
        DbConnection(currentDbPath, jdbcDriver, SourceKind.CURRENT, diagnostic)
    }

    override fun close() {
        baselineConnection.close()
        currentConnection.close()
    }
}
