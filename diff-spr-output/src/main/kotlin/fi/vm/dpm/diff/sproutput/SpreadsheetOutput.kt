package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.diagnostic.Diagnostic
import java.io.Closeable
import java.io.FileOutputStream
import java.nio.file.Path
import org.apache.poi.xssf.streaming.SXSSFWorkbook

class SpreadsheetOutput(
    private val outputFilePath: Path,
    private val diagnostic: Diagnostic
) : Closeable {

    override fun close() {}

    fun renderOutput(diffReport: DiffReport) {
        val wb = SXSSFWorkbook(100)
        val sh = wb.createSheet()
        val row = sh.createRow(0)
        val cell = row.createCell(0)
        cell.setCellValue("DPM diff")

        val out = FileOutputStream(outputFilePath.toFile())
        wb.write(out)
        out.close()
        wb.dispose()

        diagnostic.info("Wrote: $outputFilePath")
    }
}
