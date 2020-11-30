package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.ChangeReport
import java.io.Closeable

interface ReportGenerator : Closeable {
    override fun close()
    fun generateReport(): ChangeReport
}
