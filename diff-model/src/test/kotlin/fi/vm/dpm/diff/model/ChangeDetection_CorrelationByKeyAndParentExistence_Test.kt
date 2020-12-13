package fi.vm.dpm.diff.model

import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Suppress("UNUSED_PARAMETER")
internal class ChangeDetection_CorrelationByKeyAndParentExistence_Test : ChangeDetectionTestBase() {

    @Nested
    inner class ContextParentAndPrimeKeyWithAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "ContextParentAndPrimeKeyWithAtom",
            sectionTitle = "ContextParentAndPrimeKeyWithAtom",
            sectionDescription = "ContextParentAndPrimeKeyWithAtom",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE,
            sectionFields = listOf(
                contextParentKey,
                primeKey,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single object deletion, " +
                "CTX PK VAL | CTX PK_B VAL, " +
                "CTX PK_B VAL, " +
                "CTX::PK DELETED",

            "Whole parent tree deletion, " +
                "CTX PL VAL | CTX PK_B VAL, " +
                ", " +
                "",

            "Single object addition, " +
                "CTX PK VAL, " +
                "CTX PK VAL | CTX PK_B VAL, " +
                "CTX::PK_B ADDED",

            "Whole parent tree addition, " +
                ", " +
                "CTX PK VAL, " +
                "",

            "Whole parent tree addition with multiple objects, " +
                ", " +
                "CTX PK VAL | CTX PK_B VAL, " +
                "",

            "Single object change, " +
                "CTX PK VAL, " +
                "CTX PK VAL_B, " +
                "CTX::PK MODIFIED",

            "Multiple object changes, " +
                "CTX PK VAL | CTX PK_B VAL, " +
                "CTX PK VAL_B | CTX PK_B VAL_B, " +
                "CTX::PK MODIFIED | CTX::PK_B MODIFIED",

            "ContextParent isolates equal keys, " +
                "CTX PK VAL | CTX_B PK VAL | CTX_C PK VAL, " +
                "CTX PK VAL | CTX_C PK VAL_B, " +
                "CTX_C::PK MODIFIED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String?
        ) {
            executeChangeDetectionTest(
                baselineRecordsValues = baselineRecordsValues,
                currentRecordsValues = currentRecordsValues,
                expectedResultsValues = expectedResultsValues,
                sectionOutline = sectionOutline,
                recordValueMapper = { values ->
                    mapOf(
                        contextParentKey to values[0],
                        primeKey to values[1],
                        atom to values[2]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class NormalParentAndPrimeKeyWithAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "NormalParentAndPrimeKeyWithAtom",
            sectionTitle = "NormalParentAndPrimeKeyWithAtom",
            sectionDescription = "NormalParentAndPrimeKeyWithAtom",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE,
            sectionFields = listOf(
                parentKey,
                primeKey,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single object deletion, " +
                "PAR PK VAL | PAR PK_B VAL, " +
                "PAR PK_B VAL, " +
                ":PAR:PK DELETED",

            "Whole parent tree deletion, " +
                "PAR PL VAL | PAR PK_B VAL, " +
                ", " +
                "",

            "Single object addition, " +
                "PAR PK VAL, " +
                "PAR PK VAL | PAR PK_B VAL, " +
                ":PAR:PK_B ADDED",

            "Whole parent tree addition, " +
                ", " +
                "PAR PK VAL, " +
                "",

            "Whole parent tree addition with multiple objects, " +
                ", " +
                "PAR PK VAL | PAR PK_B VAL, " +
                "",

            "Single object change, " +
                "PAR PK VAL, " +
                "PAR PK VAL_B, " +
                ":PAR:PK MODIFIED",

            "Multiple object changes, " +
                "PAR PK VAL | PAR PK_B VAL, " +
                "PAR PK VAL_B | PAR PK_B VAL_B, " +
                ":PAR:PK MODIFIED | :PAR:PK_B MODIFIED",

            "Parent isolates equal keys, " +
                "PAR PK VAL | PAR_B PK VAL | PAR_C PK VAL, " +
                "PAR PK VAL | PAR_C PK VAL_B, " +
                ":PAR_C:PK MODIFIED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String?
        ) {
            executeChangeDetectionTest(
                baselineRecordsValues = baselineRecordsValues,
                currentRecordsValues = currentRecordsValues,
                expectedResultsValues = expectedResultsValues,
                sectionOutline = sectionOutline,
                recordValueMapper = { values ->
                    mapOf(
                        parentKey to values[0],
                        primeKey to values[1],
                        atom to values[2]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class TwoKeysPerKeyFieldKindAndAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "TwoKeysPerKeyFieldKindAndAtom ",
            sectionTitle = "TwoKeysPerKeyFieldKindAndAtom ",
            sectionDescription = "TwoKeysPerKeyFieldKindAndAtom ",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE,
            sectionFields = listOf(
                contextParentKey,
                contextParentKey2,
                parentKey,
                parentKey2,
                primeKey,
                primeKey2,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "First ContextParent isolates keys, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX_B CTX2 PAR PAR2 PK PK2 VAL, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX_B CTX2 PAR PAR2 PK PK2 VAL_B, " +
                "CTX_B/CTX2:PAR/PAR2:PK/PK2 MODIFIED",

            "Second ContextParent isolates keys, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2_B PAR PAR2 PK PK2 VAL, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2_B PAR PAR2 PK PK2 VAL_B, " +
                "CTX/CTX2_B:PAR/PAR2:PK/PK2 MODIFIED",

            "First Parent isolates keys, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2 PAR_B PAR2 PK PK2 VAL, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2 PAR_B PAR2 PK PK2 VAL_B, " +
                "CTX/CTX2:PAR_B/PAR2:PK/PK2 MODIFIED",

            "Second Parent isolates keys, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2 PAR PAR2_B PK PK2 VAL, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2 PAR PAR2_B PK PK2 VAL_B, " +
                "CTX/CTX2:PAR/PAR2_B:PK/PK2 MODIFIED",

            "First prime isolates keys, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2 PAR PAR2 PK_B PK2 VAL, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2 PAR PAR2 PK_B PK2 VAL_B, " +
                "CTX/CTX2:PAR/PAR2:PK_B/PK2 MODIFIED",

            "Second prime isolates keys, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2 PAR PAR2 PK PK2_B VAL, " +
                "CTX CTX2 PAR PAR2 PK PK2 VAL | CTX CTX2 PAR PAR2 PK PK2_B VAL_B, " +
                "CTX/CTX2:PAR/PAR2:PK/PK2_B MODIFIED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String?
        ) {
            executeChangeDetectionTest(
                baselineRecordsValues = baselineRecordsValues,
                currentRecordsValues = currentRecordsValues,
                expectedResultsValues = expectedResultsValues,
                sectionOutline = sectionOutline,
                recordValueMapper = { values ->
                    mapOf(
                        contextParentKey to values[0],
                        contextParentKey2 to values[1],
                        parentKey to values[2],
                        parentKey2 to values[3],
                        primeKey to values[4],
                        primeKey2 to values[5],
                        atom to values[6]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }
}
