package fi.vm.dpm.diff.model

import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Suppress("UNUSED_PARAMETER")
internal class ChangeDetection_CorrelationKeysUnique_Test : ChangeDetectionTestBase() {

    @Nested
    inner class PrimeSegment {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "PrimeSegment",
            sectionTitle = "PrimeSegment",
            sectionDescription = "PrimeSegment",
            sectionFields = listOf(
                primeKeySegment,
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
                ":PK: DELETED",

            "Single record addition, " +
                ", " +
                "PK, " +
                ":PK: ADDED",

            "Multiple record deletion, " +
                "PK | PK_B | PK_C, " +
                ", " +
                ":PK: DELETED | :PK_B: DELETED | :PK_C: DELETED",

            "Multiple record addition, " +
                ", " +
                "PK | PK_B | PK_C, " +
                ":PK: ADDED | :PK_B: ADDED | :PK_C: ADDED"
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
                sectionDescriptor = sectionDescriptor,
                recordValueMapper = { values ->
                    mapOf(
                        primeKeySegment to values[0]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class PrimeSegmentWithAtom {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "PrimeSegmentWithAtom",
            sectionTitle = "PrimeSegmentWithAtom",
            sectionDescription = "PrimeSegmentWithAtom",
            sectionFields = listOf(
                primeKeySegment,
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
                ":PK: DELETED",

            "Single record addition, " +
                ", " +
                "PK VAL, " +
                ":PK: ADDED",

            "Single record changes, " +
                "PK VAL, " +
                "PK VAL_B, " +
                ":PK: MODIFIED",

            "Multiple record changes, " +
                "PK VAL | PK_B VAL, " +
                "PK VAL_B | PK_B VAL_B, " +
                ":PK: MODIFIED | :PK_B: MODIFIED"
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
                sectionDescriptor = sectionDescriptor,
                recordValueMapper = { values ->
                    mapOf(
                        primeKeySegment to values[0],
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
    inner class ScopeAndPrimeSegment {
        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "ScopeAndPrimeSegment",
            sectionTitle = "ScopeAndPrimeSegment",
            sectionDescription = "ScopeAndPrimeSegment",
            sectionFields = listOf(
                scopeKeySegment,
                primeKeySegment,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "SCOPE PK, " +
                ", " +
                "SCOPE:PK: DELETED",

            "Single record addition, " +
                ", " +
                "SCOPE PK, " +
                "SCOPE:PK: ADDED",

            "Multiple record deletion, " +
                "SCOPE PK | SCOPE PK_B | SCOPE PK_C, " +
                ", " +
                "SCOPE:PK: DELETED | SCOPE:PK_B: DELETED | SCOPE:PK_C: DELETED",

            "Multiple record addition, " +
                ", " +
                "SCOPE PK | SCOPE PK_B | SCOPE PK_C, " +
                "SCOPE:PK: ADDED | SCOPE:PK_B: ADDED | SCOPE:PK_C: ADDED"
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
                sectionDescriptor = sectionDescriptor,
                recordValueMapper = { values ->
                    mapOf(
                        scopeKeySegment to values[0],
                        primeKeySegment to values[1]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class ScopeAndPrimeSegmentWithAtom {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "ScopeAndPrimeSegmentWithAtom",
            sectionTitle = "ScopeAndPrimeSegmentWithAtom",
            sectionDescription = "ScopeAndPrimeSegmentWithAtom",
            sectionFields = listOf(
                scopeKeySegment,
                primeKeySegment,
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
                "SCOPE PK VAL, " +
                ", " +
                "SCOPE:PK: DELETED",

            "Single record addition, " +
                ", " +
                "SCOPE PK VAL, " +
                "SCOPE:PK: ADDED",

            "Single record changes, " +
                "SCOPE PK VAL, " +
                "SCOPE PK VAL_B, " +
                "SCOPE:PK: MODIFIED",

            "Multiple record changes, " +
                "SCOPE PK VAL | SCOPE PK_B VAL, " +
                "SCOPE PK VAL_B | SCOPE PK_B VAL_B, " +
                "SCOPE:PK: MODIFIED | SCOPE:PK_B: MODIFIED",

            "Scope isolates equal object keys, " +
                "SCOPE PK VAL | SCOPE_B PK VAL | SCOPE_C PK VAL, " +
                "SCOPE PK VAL | SCOPE_C PK VAL_B, " +
                "SCOPE_B:PK: DELETED | SCOPE_C:PK: MODIFIED"
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
                sectionDescriptor = sectionDescriptor,
                recordValueMapper = { values ->
                    mapOf(
                        scopeKeySegment to values[0],
                        primeKeySegment to values[1],
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
    inner class PrimeAndSubSegmentWithAtom {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "PrimeAndSubSegmentWithAtom",
            sectionTitle = "PrimeAndSubSegmentWithAtom",
            sectionDescription = "PrimeAndSubSegmentWithAtom",
            sectionFields = listOf(
                primeKeySegment,
                subKeySegment,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single sub-object deletion, " +
                "PK SK VAL | PK SK_B VAL, " +
                "PK SK_B VAL, " +
                ":PK:SK DELETED",

            "Single whole record deletion, " +
                "PK SK VAL | PK SK_B VAL, " +
                ", " +
                "",

            "Single sub-object addition, " +
                "PK SK VAL, " +
                "PK SK VAL | PK SK_B VAL, " +
                ":PK:SK_B ADDED",

            "Single whole record addition, " +
                ", " +
                "PK SK VAL, " +
                "",

            "Single sub-object change, " +
                "PK SK VAL, " +
                "PK SK VAL_B, " +
                ":PK:SK MODIFIED",

            "Multiple sub-object changes, " +
                "PK SK VAL | PK SK_B VAL, " +
                "PK SK VAL_B | PK SK_B VAL_B, " +
                ":PK:SK MODIFIED | :PK:SK_B MODIFIED"
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
                sectionDescriptor = sectionDescriptor,
                recordValueMapper = { values ->
                    mapOf(
                        primeKeySegment to values[0],
                        subKeySegment to values[1],
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
    inner class ScopePrimeAndSubSegmentWithAtom {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "ScopePrimeAndSubSegmentWithAtom",
            sectionTitle = "ScopePrimeAndSubSegmentWithAtom",
            sectionDescription = "ScopePrimeAndSubSegmentWithAtom",
            sectionFields = listOf(
                scopeKeySegment,
                primeKeySegment,
                subKeySegment,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single sub-object deletion, " +
                "SCOPE PK SK SK VAL | SCOPE PK SK_B VAL, " +
                "SCOPE PK SK_B VAL, " +
                "SCOPE:PK:SK DELETED",

            "Single whole record deletion, " +
                "SCOPE PK SK VAL | SCOPE PK SK_B VAL, " +
                ", " +
                "",

            "Single sub-object addition, " +
                "SCOPE PK SK VAL, " +
                "SCOPE PK SK VAL | SCOPE PK SK_B VAL, " +
                "SCOPE:PK:SK_B ADDED",

            "Single whole record addition, " +
                ", " +
                "SCOPE PK SK VAL, " +
                "",

            "Single sub-object change, " +
                "SCOPE PK SK VAL, " +
                "SCOPE PK SK VAL_B, " +
                "SCOPE:PK:SK MODIFIED",

            "Multiple sub-object changes, " +
                "SCOPE PK SK VAL | SCOPE PK SK_B VAL, " +
                "SCOPE PK SK VAL_B | SCOPE PK SK_B VAL_B, " +
                "SCOPE:PK:SK MODIFIED | SCOPE:PK:SK_B MODIFIED",

            "Scope isolates equal object keys, " +
                "SCOPE PK SK VAL | SCOPE_B PK SK VAL | SCOPE_C PK SK VAL, " +
                "SCOPE PK SK VAL | SCOPE_C PK SK VAL_B, " +
                "SCOPE_C:PK:SK MODIFIED"
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
                sectionDescriptor = sectionDescriptor,
                recordValueMapper = { values ->
                    mapOf(
                        scopeKeySegment to values[0],
                        primeKeySegment to values[1],
                        subKeySegment to values[2],
                        atom to values[3]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class TwoScopePrimeAndSubSegmentsWithAtom {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "TwoScopePrimeAndSubSegmentsWithAtom ",
            sectionTitle = "TwoScopePrimeAndSubSegmentsWithAtom ",
            sectionDescription = "TwoScopePrimeAndSubSegmentsWithAtom ",
            sectionFields = listOf(
                scopeKeySegment,
                scopeKeySegment2,
                primeKeySegment,
                primeKeySegment2,
                subKeySegment,
                subKeySegment2,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allChanges()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "First scope segment isolates keys, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE_B SCOPE2 PK PK2 SK SK2 VAL, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE_B SCOPE2 PK PK2 SK SK2 VAL_B, " +
                "SCOPE_B/SCOPE2:PK/PK2:SK/SK2 MODIFIED",

            "Second scope segment isolates keys, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2_B PK PK2 SK SK2 VAL, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2_B PK PK2 SK SK2 VAL_B, " +
                "SCOPE/SCOPE2_B:PK/PK2:SK/SK2 MODIFIED",

            "First prime segment isolates keys, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK_B PK2 SK SK2 VAL, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK_B PK2 SK SK2 VAL_B, " +
                "SCOPE/SCOPE2:PK_B/PK2:SK/SK2 MODIFIED",

            "Second prime segment isolates keys, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK PK2_B SK SK2 VAL, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK PK2_B SK SK2 VAL_B, " +
                "SCOPE/SCOPE2:PK/PK2_B:SK/SK2 MODIFIED",

            "First sub object segment isolates keys, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK PK2 SK_B SK2 VAL, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK PK2 SK_B SK2 VAL_B, " +
                "SCOPE/SCOPE2:PK/PK2:SK_B/SK2 MODIFIED",

            "Second sub object segment isolates keys, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK PK2 SK SK2_B VAL, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK PK2 SK SK2_B VAL_B, " +
                "SCOPE/SCOPE2:PK/PK2:SK/SK2_B MODIFIED"
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
                sectionDescriptor = sectionDescriptor,
                recordValueMapper = { values ->
                    mapOf(
                        scopeKeySegment to values[0],
                        scopeKeySegment2 to values[1],
                        primeKeySegment to values[2],
                        primeKeySegment2 to values[3],
                        subKeySegment to values[4],
                        subKeySegment2 to values[5],
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
