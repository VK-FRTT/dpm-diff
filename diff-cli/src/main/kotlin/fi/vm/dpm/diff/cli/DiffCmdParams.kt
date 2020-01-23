package fi.vm.dpm.diff.cli

import java.nio.file.Path

data class DiffCmdParams(
    val baselineDpmDb: Path,
    val changedDpmDb: Path,
    val reportConfig: Path
)
