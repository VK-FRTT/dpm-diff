package fi.vm.dpm.diff.cli

import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.DriverManager
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class DpmDiffCli_TestBase {
    protected lateinit var tempFolder: TempFolder
    protected lateinit var baselineDbPath: Path
    protected lateinit var currentDbPath: Path
    protected lateinit var outputSpreadsheetPath: Path

    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: DiffCli

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("dpmdiff_cli")
        baselineDbPath = tempFolder.resolve("baseline.db")
        currentDbPath = tempFolder.resolve("current.db")
        outputSpreadsheetPath = tempFolder.resolve("output.xlsx")

        charset = StandardCharsets.UTF_8
        outCollector = PrintStreamCollector(charset)
        errCollector = PrintStreamCollector(charset)

        cli = DiffCli(
            outStream = outCollector.printStream(),
            errStream = errCollector.printStream(),
            charset = charset
        )
    }

    @AfterEach
    fun baseTeardown() {
        tempFolder.close()
    }

    protected fun executeVkDataCompareWithSpreadsheetOutputAndExpectSuccess(
        section: String,
        baselineInitSql: String,
        currentInitSql: String,
        verification: (String, Map<String, List<List<String>>>) -> Unit
    ) {
        setupSourceDb(
            targetDbPath = baselineDbPath,
            seedResourceName = "/db_fixture/empty_vkdata.db",
            initSql = baselineInitSql
        )

        setupSourceDb(
            targetDbPath = currentDbPath,
            seedResourceName = "/db_fixture/empty_vkdata.db",
            initSql = currentInitSql
        )

        val args = arrayOf(
            "--compareVkData",
            "--baselineDb",
            "$baselineDbPath",
            "--currentDb",
            "$currentDbPath",
            "--output",
            "$outputSpreadsheetPath",
            "--verbosity",
            "DEBUG",
            "--reportSections",
            "$section"
        )

        executeCliAndExpectSuccess(args) { outText ->
            verification(
                outText,
                readWorkbookContent(outputSpreadsheetPath)
            )
        }
    }

    protected fun executeCliAndExpectSuccess(args: Array<String>, verifier: (String) -> Unit) {
        val result = executeCli(args)

        assertThat(result.errText).isBlank()

        verifier(result.outText)

        assertThat(result.status).isEqualTo(DPM_DIFF_CLI_SUCCESS)
    }

    private fun executeCli(args: Array<String>): ExecuteResult {
        val status = cli.execute(args)

        val result = ExecuteResult(
            status,
            outCollector.grabText(),
            errCollector.grabText()
        )

        return result
    }

    private class PrintStreamCollector(val charset: Charset) {
        private val baos = ByteArrayOutputStream()
        private val ps = PrintStream(baos, true, charset.name())

        fun printStream(): PrintStream = ps

        fun grabText(): String {
            ps.close()
            return String(baos.toByteArray(), charset)
        }
    }

    private data class ExecuteResult(
        val status: Int,
        val outText: String,
        val errText: String
    )

    private fun setupSourceDb(
        targetDbPath: Path,
        seedResourceName: String,
        initSql: String
    ) {
        val stream = this::class.java.getResourceAsStream(seedResourceName)
        Files.copy(stream, targetDbPath, StandardCopyOption.REPLACE_EXISTING)

        DriverManager.getConnection("jdbc:sqlite:$targetDbPath").use { dbConnection ->
            dbConnection.createStatement().executeUpdate(initSql)
        }
    }

    private fun readWorkbookContent(workbookPath: Path): Map<String, List<List<String>>> {

        val inputStream = FileInputStream(workbookPath.toFile())

        return inputStream.use { inp ->

            val poiWorkbook = WorkbookFactory.create(inp)

            val workbookContent = poiWorkbook.map { poiSheet ->

                val sheetContent = poiSheet.mapNotNull { poiRow ->

                    val rowContent = poiRow.map { poiCell ->
                        poiCell.stringCellValue
                    }

                    if (rowContent.isEmpty()) {
                        null
                    } else {
                        rowContent
                    }
                }

                poiSheet.sheetName to sheetContent
            }.toMap()

            workbookContent
        }
    }
}

private val NEWLINE_PATTERN = "[\\r\\n]+".toRegex()

fun Map<String, List<List<String>>>.sectionSheetAsStringList(sheetName: String): List<String> {

    val sheetContent = this[sheetName]

    sheetContent ?: return listOf("No sheet with name: $sheetName")

    return sheetContent.mapIndexed { index, rowContent ->

        if (index == 0) {
            rowContent.joinToString(
                separator = ", #",
                prefix = "#"
            ) { cellContent ->
                cellContent.replace(NEWLINE_PATTERN, " ")
            }
        } else {
            rowContent.joinToString(
                separator = ", ",
                prefix = "",
                postfix = ""
            ) { cellContent ->
                cellContent.replace(NEWLINE_PATTERN, " ")
            }
        }
    }
}
