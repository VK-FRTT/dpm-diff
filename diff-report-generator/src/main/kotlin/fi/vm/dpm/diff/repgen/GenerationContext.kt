package fi.vm.dpm.diff.repgen

import java.sql.Connection

data class GenerationContext(
    val baselineConnection: Connection,
    val actualConnection: Connection
)
