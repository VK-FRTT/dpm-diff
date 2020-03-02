package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.diagnostic.Diagnostic

data class GenerationContext(
    val baselineConnection: DbConnection,
    val currentConnection: DbConnection,
    val identificationLabelLangCodes: List<String>,
    val diagnostic: Diagnostic
)
