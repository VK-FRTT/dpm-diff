package fi.vm.dpm.diff.cli

import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.DriverManager
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class DpmDiffCli_CompareTestBase(
    val section: String,
    val commonSetupSql: String
) : DpmDiffCli_TestBase() {

    private lateinit var tempFolder: TempFolder
    private lateinit var baselineDbPath: Path
    private lateinit var currentDbPath: Path
    private lateinit var outputSpreadsheetPath: Path

    @BeforeEach
    fun compareTestBaseInit() {
        tempFolder = TempFolder("dpmdiff_cli")
        baselineDbPath = tempFolder.resolve("baseline.db")
        currentDbPath = tempFolder.resolve("current.db")
        outputSpreadsheetPath = tempFolder.resolve("output.xlsx")
    }

    @AfterEach
    fun compareTestBaseTeardown() {
        tempFolder.close()
    }

    protected fun executeDpmCompareForSectionAndExpectSuccess(
        baselineSql: String = "",
        currentSql: String = "",
        identificationLabelLanguages: String = "fi",
        translationLanguages: String? = null,
        expectedChanges: Int,
        verifyAction: (String, Map<String, List<List<String>>>) -> Unit
    ) {
        doExecuteCompareAndExpectSuccess(
            seedResourceName = "/db_fixture/empty_dpm.db",
            commonSetupSql = commonSetupSql,
            baselineSql = baselineSql,
            currentSql = currentSql,
            args = arrayOf(
                "--compareDpm",
                "--baselineDb",
                "$baselineDbPath",
                "--currentDb",
                "$currentDbPath",
                "--output",
                "$outputSpreadsheetPath",
                "--verbosity",
                "DEBUG",
                "--identificationLabelLanguages",
                identificationLabelLanguages,
                "--translationLanguages",
                translationLanguages,
                "--reportSections",
                section
            ),
            expectedSection = section,
            expectedChanges = expectedChanges,
            verifyAction = verifyAction
        )
    }

    protected fun executeVkDataCompareForSectionAndExpectSuccess(
        baselineSql: String,
        currentSql: String,
        expectedChanges: Int,
        verifyAction: (String, Map<String, List<List<String>>>) -> Unit
    ) {
        doExecuteCompareAndExpectSuccess(
            seedResourceName = "/db_fixture/empty_vkdata.db",
            commonSetupSql = "",
            baselineSql = baselineSql,
            currentSql = currentSql,
            args = arrayOf(
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
            ),
            expectedSection = section,
            expectedChanges = expectedChanges,
            verifyAction = verifyAction
        )
    }

    private fun doExecuteCompareAndExpectSuccess(
        seedResourceName: String,
        commonSetupSql: String,
        baselineSql: String,
        currentSql: String,
        args: Array<String?>,
        expectedSection: String,
        expectedChanges: Int,
        verifyAction: (String, Map<String, List<List<String>>>) -> Unit
    ) {
        setupSourceDb(
            targetDbPath = baselineDbPath,
            seedResourceName = seedResourceName,
            commonSetupSql = commonSetupSql,
            testCaseSql = baselineSql
        )

        setupSourceDb(
            targetDbPath = currentDbPath,
            seedResourceName = seedResourceName,
            commonSetupSql = commonSetupSql,
            testCaseSql = currentSql
        )

        val validArgs = filterNonNullArgs(args)

        executeCliAndExpectSuccess(validArgs) { outText ->

            Assertions.assertThat(outText).containsSubsequence(
                "Writing report to:",
                "Report done!"
            )

            Assertions.assertThat(outText).containsSubsequence(
                "Section: $expectedSection",
                "Total changes: $expectedChanges"
            )

            verifyAction(
                outText,
                readWorkbookContent(outputSpreadsheetPath)
            )
        }
    }

    private fun setupSourceDb(
        targetDbPath: Path,
        seedResourceName: String,
        commonSetupSql: String,
        testCaseSql: String
    ) {
        val stream = this::class.java.getResourceAsStream(seedResourceName)
        Files.copy(stream, targetDbPath, StandardCopyOption.REPLACE_EXISTING)

        DriverManager.getConnection("jdbc:sqlite:$targetDbPath").use { dbConnection ->

            if (commonSetupSql.isNotEmpty()) {
                dbConnection.createStatement().executeUpdate(commonSetupSql)
            }

            if (testCaseSql.isNotEmpty()) {
                dbConnection.createStatement().executeUpdate(testCaseSql)
            }
        }
    }

    private fun filterNonNullArgs(args: Array<String?>): Array<String> {

        var skipArgNameArg = false

        return args
            .reversed()
            .mapNotNull { arg ->
                when {
                    skipArgNameArg -> {
                        skipArgNameArg = false
                        null
                    }
                    arg == null -> {
                        skipArgNameArg = true
                        null
                    }
                    else -> {
                        arg
                    }
                }
            }
            .reversed()
            .toTypedArray()
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

    private val NEWLINE_PATTERN = "[\\r\\n]+".toRegex()

    fun Map<String, List<List<String>>>.transposeSectionSheetAsList(sheetName: String): List<String> {

        val sheetContent =
            this[sheetName] ?: return listOf("No $sheetName sheet, has sheets: ${keys.joinToString()}")
        if (sheetContent.isEmpty()) return listOf("No rows on sheet: $sheetName")

        val columnTitles = sheetContent[0].map { titleCellContent ->
            val formattedTitle = titleCellContent
                .toLowerCase()
                .split(" ")
                .joinToString(separator = "") {
                    it.capitalize()
                }

            "$formattedTitle: "
        }

        if (sheetContent.size == 1) return columnTitles

        return sheetContent
            .drop(1)
            .map { rowContent ->
                rowContent.mapIndexed { cellIndex, cellContent ->
                    "${columnTitles[cellIndex]}${cellContent.replace(NEWLINE_PATTERN, " #")}"
                }
            }
            .intersperse(listOf("-----------"))
            .flatten()
    }

    private fun <T> Iterable<T>.intersperse(separator: T): List<T> {
        val result = ArrayList<T>()
        var count = 0

        forEach {
            if (++count > 1) result.add(separator)
            result.add(it)
        }

        return result
    }
}
