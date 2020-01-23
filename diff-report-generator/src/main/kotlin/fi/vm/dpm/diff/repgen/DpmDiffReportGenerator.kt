package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import java.io.Closeable
import java.nio.file.Path

class DpmDiffReportGenerator(
    private val baselineDpmDbPath: Path,
    private val actualDpmDbPath: Path,
    diagnostic: Diagnostic
) : Closeable {

    override fun close() {}

    fun generateReport(): DiffReport {

        return DiffReport()
    }
}
