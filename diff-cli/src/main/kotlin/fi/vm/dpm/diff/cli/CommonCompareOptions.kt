package fi.vm.dpm.diff.cli

import java.nio.file.Path

data class CommonCompareOptions(
    val baselineDbPath: Path,
    val currentDbPath: Path,
    val outputFilePath: Path
)
