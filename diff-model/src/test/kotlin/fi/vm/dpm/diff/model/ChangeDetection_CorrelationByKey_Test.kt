package fi.vm.dpm.diff.model

import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Suppress("UNUSED_PARAMETER")
internal class ChangeDetection_CorrelationByKey_Test : ChangeDetectionTestBase() {

    @Nested
    inner class PrimeKey {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "PrimeKey",
            sectionTitle = "PrimeKey",
            sectionDescription = "PrimeKey",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                primeKey,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "PK, " +
                ", " +
                "::PK DELETED",

            "Single record addition, " +
                ", " +
                "PK, " +
                "::PK ADDED",

            "Multiple record deletion, " +
                "PK | PK_B | PK_C, " +
                ", " +
                "::PK DELETED | ::PK_B DELETED | ::PK_C DELETED",

            "Multiple record addition, " +
                ", " +
                "PK | PK_B | PK_C, " +
                "::PK ADDED | ::PK_B ADDED | ::PK_C ADDED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String
        ) {
            executeChangeDetectionTest(
                baselineRecordsValues = baselineRecordsValues,
                currentRecordsValues = currentRecordsValues,
                expectedResultsValues = expectedResultsValues,
                sectionOutline = sectionOutline,
                recordValueMapper = { values ->
                    mapOf(
                        primeKey to values[0]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class PrimeKeyWithAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "PrimeKeyWithAtom",
            sectionTitle = "PrimeKeyWithAtom",
            sectionDescription = "PrimeKeyWithAtom",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
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
            "Single record deletion, " +
                "PK VAL, " +
                ", " +
                "::PK DELETED",

            "Single record addition, " +
                ", " +
                "PK VAL, " +
                "::PK ADDED",

            "Single record changes, " +
                "PK VAL, " +
                "PK VAL_B, " +
                "::PK MODIFIED",

            "Multiple record changes, " +
                "PK VAL | PK_B VAL, " +
                "PK VAL_B | PK_B VAL_B, " +
                "::PK MODIFIED | ::PK_B MODIFIED",

            "Record changes with duplicate record keys in baseline, " +
                "PK VAL | PK VAL_B, " +
                "PK VAL_C, " +
                "::PK DUPLICATE_KEY_ALERT | ::PK DUPLICATE_KEY_ALERT",

            "Record changes with duplicate record keys in current, " +
                "PK VAL, " +
                "PK VAL_B | PK VAL_C, " +
                "::PK DUPLICATE_KEY_ALERT | ::PK DUPLICATE_KEY_ALERT"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String
        ) {
            executeChangeDetectionTest(
                baselineRecordsValues = baselineRecordsValues,
                currentRecordsValues = currentRecordsValues,
                expectedResultsValues = expectedResultsValues,
                sectionOutline = sectionOutline,
                recordValueMapper = { values ->
                    mapOf(
                        primeKey to values[0],
                        atom to values[1]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class ContextParentAndPrimeKey {
        private val sectionOutline = SectionOutline(
            sectionShortTitle = "ContextParentAndPrimeKey",
            sectionTitle = "ContextParentAndPrimeKey",
            sectionDescription = "ContextParentAndPrimeKey",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                contextParentKey,
                primeKey,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "CTX PK, " +
                ", " +
                "CTX::PK DELETED",

            "Single record addition, " +
                ", " +
                "CTX PK, " +
                "CTX::PK ADDED",

            "Multiple record deletion, " +
                "CTX PK | CTX PK_B | CTX PK_C, " +
                ", " +
                "CTX::PK DELETED | CTX::PK_B DELETED | CTX::PK_C DELETED",

            "Multiple record addition, " +
                ", " +
                "CTX PK | CTX PK_B | CTX PK_C, " +
                "CTX::PK ADDED | CTX::PK_B ADDED | CTX::PK_C ADDED",

            "ContextParent isolates equal prime keys, " +
                "CTX PK | CTX_B PK | CTX_C PK, " +
                "CTX PK | CTX_C PK, " +
                "CTX_B::PK DELETED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String
        ) {
            executeChangeDetectionTest(
                baselineRecordsValues = baselineRecordsValues,
                currentRecordsValues = currentRecordsValues,
                expectedResultsValues = expectedResultsValues,
                sectionOutline = sectionOutline,
                recordValueMapper = { values ->
                    mapOf(
                        contextParentKey to values[0],
                        primeKey to values[1]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class ContextParentAndPrimeKeyWithAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "ContextParentAndPrimeKeyWithAtom",
            sectionTitle = "ContextParentAndPrimeKeyWithAtom",
            sectionDescription = "ContextParentAndPrimeKeyWithAtom",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
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
            "Single record deletion, " +
                "CTX PK VAL, " +
                ", " +
                "CTX::PK DELETED",

            "Single record addition, " +
                ", " +
                "CTX PK VAL, " +
                "CTX::PK ADDED",

            "Single record changes, " +
                "CTX PK VAL, " +
                "CTX PK VAL_B, " +
                "CTX::PK MODIFIED",

            "Multiple record changes, " +
                "CTX PK VAL | CTX PK_B VAL, " +
                "CTX PK VAL_B | CTX PK_B VAL_B, " +
                "CTX::PK MODIFIED | CTX::PK_B MODIFIED",

            "ContextParent isolates equal prime keys, " +
                "CTX PK VAL | CTX_B PK VAL | CTX_C PK VAL, " +
                "CTX PK VAL | CTX_C PK VAL_B, " +
                "CTX_B::PK DELETED | CTX_C::PK MODIFIED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String
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
    inner class NormalParentAndPrimeKey {
        private val sectionOutline = SectionOutline(
            sectionShortTitle = "NormalParentAndPrimeKey",
            sectionTitle = "NormalParentAndPrimeKey",
            sectionDescription = "NormalParentAndPrimeKey",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                parentKey,
                primeKey,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "PAR PK, " +
                ", " +
                ":PAR:PK DELETED",

            "Single record addition, " +
                ", " +
                "PAR PK, " +
                ":PAR:PK ADDED",

            "Multiple record deletion, " +
                "PAR PK | PAR PK_B | PAR PK_C, " +
                ", " +
                ":PAR:PK DELETED | :PAR:PK_B DELETED | :PAR:PK_C DELETED",

            "Multiple record addition, " +
                ", " +
                "PAR PK | PAR PK_B | PAR PK_C, " +
                ":PAR:PK ADDED | :PAR:PK_B ADDED | :PAR:PK_C ADDED",

            "Parent isolates equal prime keys, " +
                "PAR PK | PAR_B PK | PAR_C PK, " +
                "PAR PK | PAR_C PK, " +
                ":PAR_B:PK DELETED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String
        ) {
            executeChangeDetectionTest(
                baselineRecordsValues = baselineRecordsValues,
                currentRecordsValues = currentRecordsValues,
                expectedResultsValues = expectedResultsValues,
                sectionOutline = sectionOutline,
                recordValueMapper = { values ->
                    mapOf(
                        parentKey to values[0],
                        primeKey to values[1]
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
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
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
            "Single record deletion, " +
                "PAR PK VAL, " +
                ", " +
                ":PAR:PK DELETED",

            "Single record addition, " +
                ", " +
                "PAR PK VAL, " +
                ":PAR:PK ADDED",

            "Single record changes, " +
                "PAR PK VAL, " +
                "PAR PK VAL_B, " +
                ":PAR:PK MODIFIED",

            "Multiple record changes, " +
                "PAR PK VAL | PAR PK_B VAL, " +
                "PAR PK VAL_B | PAR PK_B VAL_B, " +
                ":PAR:PK MODIFIED | :PAR:PK_B MODIFIED",

            "Parent isolates equal prime keys, " +
                "PAR PK VAL | PAR_B PK VAL | PAR_C PK VAL, " +
                "PAR PK VAL | PAR_C PK VAL_B, " +
                ":PAR_B:PK DELETED | :PAR_C:PK MODIFIED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String
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
    inner class ContextParentNormalParentAndPrimeKey {
        private val sectionOutline = SectionOutline(
            sectionShortTitle = "ContextParentNormalParentAndPrimeKey",
            sectionTitle = "ContextParentNormalParentAndPrimeKey",
            sectionDescription = "ContextParentNormalParentAndPrimeKey",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                contextParentKey,
                parentKey,
                primeKey,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "CTX PAR PK, " +
                ", " +
                "CTX:PAR:PK DELETED",

            "Single record addition, " +
                ", " +
                "CTX PAR PK, " +
                "CTX:PAR:PK ADDED",

            "Multiple record deletion, " +
                "CTX PAR PK | CTX PAR PK_B | CTX PAR PK_C, " +
                ", " +
                "CTX:PAR:PK DELETED | CTX:PAR:PK_B DELETED | CTX:PAR:PK_C DELETED",

            "Multiple record addition, " +
                ", " +
                "CTX PAR PK | CTX PAR PK_B | CTX PAR PK_C, " +
                "CTX:PAR:PK ADDED | CTX:PAR:PK_B ADDED | CTX:PAR:PK_C ADDED",

            "ContextParent isolates equal prime keys, " +
                "CTX PAR PK | CTX_B PAR PK | CTX_C PAR PK, " +
                "CTX PAR PK | CTX_C PAR PK, " +
                "CTX_B:PAR:PK DELETED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String
        ) {
            executeChangeDetectionTest(
                baselineRecordsValues = baselineRecordsValues,
                currentRecordsValues = currentRecordsValues,
                expectedResultsValues = expectedResultsValues,
                sectionOutline = sectionOutline,
                recordValueMapper = { values ->
                    mapOf(
                        contextParentKey to values[0],
                        parentKey to values[1],
                        primeKey to values[2]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class ContextParentNormalParentAndPrimeKeyWithAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "ContextParentNormalParentAndPrimeKeyWithAtom",
            sectionTitle = "ContextParentNormalParentAndPrimeKeyWithAtom",
            sectionDescription = "ContextParentNormalParentAndPrimeKeyWithAtom",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                contextParentKey,
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
            "Single record deletion, " +
                "CTX PAR PK VAL, " +
                ", " +
                "CTX:PAR:PK DELETED",

            "Single record addition, " +
                ", " +
                "CTX PAR PK VAL, " +
                "CTX:PAR:PK ADDED",

            "Single record changes, " +
                "CTX PAR PK VAL, " +
                "CTX PAR PK VAL_B, " +
                "CTX:PAR:PK MODIFIED",

            "Multiple record changes, " +
                "CTX PAR PK VAL | CTX PAR PK_B VAL, " +
                "CTX PAR PK VAL_B | CTX PAR PK_B VAL_B, " +
                "CTX:PAR:PK MODIFIED | CTX:PAR:PK_B MODIFIED",

            "ContextParent isolates equal prime keys, " +
                "CTX PAR PK VAL | CTX_B PAR PK VAL | CTX_C PAR PK VAL, " +
                "CTX PAR PK VAL | CTX_C PAR PK VAL_B, " +
                "CTX_B:PAR:PK DELETED | CTX_C:PAR:PK MODIFIED"
        )
        fun testChangeDetection(
            testName: String,
            baselineRecordsValues: String?,
            currentRecordsValues: String?,
            expectedResultsValues: String
        ) {
            executeChangeDetectionTest(
                baselineRecordsValues = baselineRecordsValues,
                currentRecordsValues = currentRecordsValues,
                expectedResultsValues = expectedResultsValues,
                sectionOutline = sectionOutline,
                recordValueMapper = { values ->
                    mapOf(
                        contextParentKey to values[0],
                        parentKey to values[1],
                        primeKey to values[2],
                        atom to values[3]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }
}
