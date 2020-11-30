package fi.vm.dpm.diff.cli

import java.nio.file.Path

data class CompareParamsCommon(
    val baselineDbPath: Path,
    val currentDbPath: Path,
    val outputFilePath: Path,
    val forceOverwrite: Boolean
)
