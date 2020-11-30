package fi.vm.dpm.diff.repgen.dpm

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.repgen.SQLiteDbConnection

data class DpmGenerationContext(
    val baselineConnection: SQLiteDbConnection,
    val currentConnection: SQLiteDbConnection,
    val identificationLabelLangCodes: List<String>,
    val diagnostic: Diagnostic
)
