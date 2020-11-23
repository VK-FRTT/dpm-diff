package fi.vm.dpm.diff.cli

import java.nio.file.Path

data class DpmDiffReportParams(
    val baselineDpmDbPath: Path,
    val currentDpmDbPath: Path,
    val outputFilePath: Path,
    val forceOverwrite: Boolean,
    val identificationLabelLangCodes: List<String>,
    val translationLangCodes: List<String>?
)
