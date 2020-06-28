package fi.vm.dpm.diff.model

import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Suppress("UNUSED_PARAMETER")
internal class ChangeDetection_CorrelationKeysUnique_Test : ChangeDetectionTestBase() {

    @Nested
    inner class PlainTopLevelSegment {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "PlainTopLevelSegment",
            sectionTitle = "PlainTopLevelSegment",
            sectionDescription = "PlainTopLevelSegment",
            sectionFields = listOf(
                topLevelKeySegment,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allValues()
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
                        topLevelKeySegment to values[0]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class PlainTopLevelSegmentWithAtom {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "PlainTopLevelSegmentWithAtom",
            sectionTitle = "PlainTopLevelSegmentWithAtom",
            sectionDescription = "PlainTopLevelSegmentWithAtom",
            sectionFields = listOf(
                topLevelKeySegment,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allValues()
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
                        topLevelKeySegment to values[0],
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
    inner class ScopedTopLevelSegment {
        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "ScopedTopLevelSegment",
            sectionTitle = "ScopedTopLevelSegment",
            sectionDescription = "ScopedTopLevelSegment",
            sectionFields = listOf(
                scopingTopLevelKeySegment,
                topLevelKeySegment,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allValues()
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
                        scopingTopLevelKeySegment to values[0],
                        topLevelKeySegment to values[1]
                    )
                },
                changeResultsMapper = { changeResults ->
                    changeResults.toKeyAndChangeKindList()
                }
            )
        }
    }

    @Nested
    inner class ScopedTopLevelSegmentWithAtom {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "ScopedTopLevelSegmentWithAtom",
            sectionTitle = "ScopedTopLevelSegmentWithAtom",
            sectionDescription = "ScopedTopLevelSegmentWithAtom",
            sectionFields = listOf(
                scopingTopLevelKeySegment,
                topLevelKeySegment,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allValues()
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

            "Scope isolates equal top level keys, " +
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
                        scopingTopLevelKeySegment to values[0],
                        topLevelKeySegment to values[1],
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
    inner class TopLevelAndSubObjectSegment {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "TopLevelAndSubObjectSegment",
            sectionTitle = "TopLevelAndSubObjectSegment",
            sectionDescription = "TopLevelAndSubObjectSegment",
            sectionFields = listOf(
                topLevelKeySegment,
                subObjectSegment,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allValues()
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
                        topLevelKeySegment to values[0],
                        subObjectSegment to values[1],
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
    inner class ScopedTopLevelAndSubObjectSegmentWithAtom {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "ScopedTopLevelAndSubObjectSegmentWithAtom",
            sectionTitle = "ScopedTopLevelAndSubObjectSegmentWithAtom",
            sectionDescription = "ScopedTopLevelAndSubObjectSegmentWithAtom",
            sectionFields = listOf(
                scopingTopLevelKeySegment,
                topLevelKeySegment,
                subObjectSegment,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allValues()
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

            "Scope isolates equal top level keys, " +
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
                        scopingTopLevelKeySegment to values[0],
                        topLevelKeySegment to values[1],
                        subObjectSegment to values[2],
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
    inner class MultipleScopedTopLevelAndSubObjectSegmentsWithAtom {

        private val sectionDescriptor = SectionDescriptor(
            sectionShortTitle = "MultipleScopedTopLevelAndSubObjectSegmentsWithAtom ",
            sectionTitle = "MultipleScopedTopLevelAndSubObjectSegmentsWithAtom ",
            sectionDescription = "MultipleScopedTopLevelAndSubObjectSegmentsWithAtom ",
            sectionFields = listOf(
                scopingTopLevelKeySegment,
                scopingTopLevelKeySegment2,
                topLevelKeySegment,
                topLevelKeySegment2,
                subObjectSegment,
                subObjectSegment2,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.allValues()
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

            "First top level segment isolates keys, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK_B PK2 SK SK2 VAL, " +
                "SCOPE SCOPE2 PK PK2 SK SK2 VAL | SCOPE SCOPE2 PK_B PK2 SK SK2 VAL_B, " +
                "SCOPE/SCOPE2:PK_B/PK2:SK/SK2 MODIFIED",

            "Second top level segment isolates keys, " +
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
                        scopingTopLevelKeySegment to values[0],
                        scopingTopLevelKeySegment2 to values[1],
                        topLevelKeySegment to values[2],
                        topLevelKeySegment2 to values[3],
                        subObjectSegment to values[4],
                        subObjectSegment2 to values[5],
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
