package fi.vm.dpm.diff.cli

import java.nio.file.Path

data class DpmDiffReportParams(
    val baselineDpmDbPath: Path,
    val currentDpmDbPath: Path,
    val reportConfig: Path,
    val outputFilePath: Path,
    val forceOverwrite: Boolean
)
