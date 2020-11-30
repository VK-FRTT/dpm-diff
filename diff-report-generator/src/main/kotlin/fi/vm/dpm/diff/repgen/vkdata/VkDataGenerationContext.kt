package fi.vm.dpm.diff.repgen.dpm

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.SQLiteDbConnection

data class VkDataGenerationContext(
    val baselineConnection: SQLiteDbConnection,
    val currentConnection: SQLiteDbConnection,
    val diagnostic: Diagnostic
)
