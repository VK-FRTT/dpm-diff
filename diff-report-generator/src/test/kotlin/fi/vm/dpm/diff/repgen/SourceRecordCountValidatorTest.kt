package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.SourceKind
import fi.vm.dpm.diff.repgen.dpm.utils.SourceTableDescriptor
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SourceRecordCountValidatorTest {

    private lateinit var diagnosticCollector: DiagnosticCollector
    private lateinit var tempFolderPath: Path
    private lateinit var dbPath: Path
    private lateinit var dbConnection: DbConnection

    @BeforeEach
    fun setup() {
        diagnosticCollector = DiagnosticCollector()

        tempFolderPath = Files.createTempDirectory("sr_count_validation_test")
        dbPath = tempFolderPath.resolve("test.db")

        DriverManager
            .getConnection("jdbc:sqlite:$dbPath")
            .use { dbConnection ->
                dbConnection.createStatement().executeUpdate(testSetupSql())
            }

        dbConnection = DbConnection(
            dbPath,
            "sqlite",
            SourceKind.BASELINE,
            diagnosticCollector
        )
    }

    @AfterEach
    fun teardown() {
        dbConnection.close()

        Files
            .walk(tempFolderPath)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    @Test
    fun `SourceTableDescriptor with single plain string table name should produce total row count`() {

        val sourceTableDescriptors = listOf(
            "mDomain"
        )

        val thrown = Assertions.catchThrowable {
            SourceRecordCountValidator.validateCountWithSourceTableTotalRows(
                2,
                sourceTableDescriptors,
                dbConnection,
                "TestSection",
                diagnosticCollector
            )
        }

        assertThat(diagnosticCollector.messages).contains("DEBUG: SourceRecord count validation (BASELINE): OK")
        assertThat(thrown).isNull()
    }

    @Test
    fun `SourceTableDescriptor with two plain string table names should produce total row count`() {

        val sourceTableDescriptors = listOf(
            "mDomain",
            "mMember"
        )

        val thrown = Assertions.catchThrowable {
            SourceRecordCountValidator.validateCountWithSourceTableTotalRows(
                2 + 3,
                sourceTableDescriptors,
                dbConnection,
                "TestSection",
                diagnosticCollector
            )
        }

        assertThat(diagnosticCollector.messages).contains("DEBUG: SourceRecord count validation (BASELINE): OK")
        assertThat(thrown).isNull()
    }

    @Test
    fun `SourceTableDescriptor with join & where criteria should produce total row count`() {

        val sourceTableDescriptors = listOf(
                SourceTableDescriptor(
                    table = "mDomain",
                    joins = listOf(
                        "mConceptTranslation on mConceptTranslation.ConceptID = mDomain.ConceptID",
                        "mLanguage on mLanguage.LanguageID = mConceptTranslation.LanguageID"
                    ),
                    where = "mLanguage.IsoCode IN ('fi', 'sv', 'en')"
                )
        )

        val thrown = Assertions.catchThrowable {
            SourceRecordCountValidator.validateCountWithSourceTableTotalRows(
                3,
                sourceTableDescriptors,
                dbConnection,
                "TestSection",
                diagnosticCollector
            )
        }

        assertThat(diagnosticCollector.messages).contains("DEBUG: SourceRecord count validation (BASELINE): OK")
        assertThat(thrown).isNull()
    }

    @Test
    fun `Should emit diagnostic message when counts do not match`() {

        val sourceTableDescriptors = listOf(
            "mDomain"
        )

        val thrown = Assertions.catchThrowable {
            SourceRecordCountValidator.validateCountWithSourceTableTotalRows(
                0,
                sourceTableDescriptors,
                dbConnection,
                "TestSection",
                diagnosticCollector
            )
        }

        assertThat(diagnosticCollector.messages[1]).containsSubsequence(
            "FATAL: SourceRecord count validation (BASELINE): Fail",
            "- Count mismatch between loaded source records and source table rows",
            "- Section: TestSection",
            "- Database: ",
            "- Loaded SourceRecords count: 0",
            "- SourceTable(s) total row count: 2"
        )

        assertThat(thrown).isNotNull()
    }

    @Test
    fun `SourceTableDescriptor with unsupported descriptor type should cause failure`() {

        val sourceTableDescriptors = listOf(
            Pair("foo", "bar")
        )

        val thrown = Assertions.catchThrowable {
            SourceRecordCountValidator.validateCountWithSourceTableTotalRows(
                0,
                sourceTableDescriptors,
                dbConnection,
                "TestSection",
                diagnosticCollector
            )
        }

        assertThat(thrown).hasMessage("Unsupported SourceTableDescriptor type: Pair")
    }

    private fun testSetupSql() =
        """
        BEGIN;

        CREATE TABLE 'mLanguage' (
            LanguageID INTEGER,
            IsoCode TEXT
            );

        CREATE TABLE 'mConcept' (
            ConceptID INTEGER,
            ConceptType TEXT
            );

        CREATE TABLE 'mConceptTranslation' (
            ConceptID INTEGER,
            LanguageID INTEGER,
            Text TEXT,
            Role TEXT
            );

        CREATE TABLE 'mDomain' (
            DomainID INTEGER,
            DomainCode TEXT,
            ConceptID INTEGER
            );

        CREATE TABLE 'mMember' (
            MemberID INTEGER,
            MemberCode  TEXT
            );


        INSERT INTO 'mLanguage' (
            LanguageID,
            IsoCode
            )
        VALUES
          (1, 'fi'),
          (2, 'sv'),
          (3, 'en'),
          (4, 'pl');


        INSERT INTO 'mConcept' (
            ConceptID,
            ConceptType
            )
        VALUES
            (1, 'Domain'),
            (2, 'Domain');


        INSERT INTO 'mConceptTranslation' (
            ConceptID,
            LanguageID,
            Text,
            Role
            )
        VALUES
            (1, 1, 'Explicit domain A (label fi)', 'label'),
            (1, 2, 'Explicit domain A (label sv)', 'label'),
            (1, 3, 'Explicit domain A (label en)', 'label'),
            (1, 4, 'Explicit domain A (label pl)', 'label');


        INSERT INTO 'mDomain' (
            DomainID,
            DomainCode,
            ConceptID
            )
        VALUES
            (101, 'EDA', 1),
            (102, 'EDB', 2);


        INSERT INTO 'mMember' (
            MemberID,
            MemberCode
            )
        VALUES
            (201, 'EDA-M1'),
            (202, 'EDA-M2'),
            (203, 'EDA-M3');


        COMMIT;

        """.trimIndent()
}
