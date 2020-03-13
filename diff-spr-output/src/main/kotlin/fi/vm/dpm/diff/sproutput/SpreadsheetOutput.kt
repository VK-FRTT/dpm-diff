package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import fi.vm.dpm.diff.sproutput.CellStyles
import fi.vm.dpm.diff.sproutput.ContentsSheet
import fi.vm.dpm.diff.sproutput.SectionSheet
import java.io.Closeable
import java.io.FileOutputStream
import java.nio.file.Path
import org.apache.poi.xssf.streaming.SXSSFWorkbook

class SpreadsheetOutput(
    private val outputFilePath: Path,
    private val diagnostic: Diagnostic
) : Closeable {

    private val workbook = SXSSFWorkbook()
    private val cellStyles = CellStyles.initCellStylesToWorkbook(workbook)

    override fun close() {
        workbook.dispose()
    }

    fun renderOutput(changeReport: ChangeReport) {
        diagnostic.info("Writing spreadsheet...")

        ContentsSheet.renderToWorkbook(
            workbook,
            cellStyles,
            changeReport
        )

        changeReport.sections.forEachIndexed { index, section ->
            SectionSheet.renderToWorkbook(
                workbook,
                cellStyles,
                section,
                index
            )
        }

        val out = FileOutputStream(outputFilePath.toFile())
        workbook.write(out)
        out.close()

        diagnostic.info("Wrote: $outputFilePath")
    }
}
