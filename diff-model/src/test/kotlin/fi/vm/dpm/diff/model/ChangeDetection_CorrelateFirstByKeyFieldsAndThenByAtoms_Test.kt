package fi.vm.dpm.diff.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Suppress("UNUSED_PARAMETER")
internal class ChangeDetection_CorrelateFirstByKeyFieldsAndThenByAtoms_Test : ChangeDetectionTestBase() {

    @Nested
    inner class PrimeKey {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "PrimeKey",
            sectionTitle = "PrimeKey",
            sectionDescription = "PrimeKey",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                primeKey,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.additionAndDeletion()
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
                "PK | PK_B | PK_C, " +
                ", " +
                "::PK DELETED | ::PK_B DELETED | ::PK_C DELETED",

            "Multiple record addition, " +
                ", " +
                "PK | PK_B | PK_C, " +
                "::PK ADDED | ::PK_B ADDED | ::PK_C ADDED"
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
                changeRecordMapper = { it.toKeyFieldsAndChangeKindString() }
            )
        }

        @Test
        fun `change detection should fail with error when ChangeKind MODIFIED is used`() {

            val sectionOutlineWithModifiedChangeKind = SectionOutline(
                sectionShortTitle = "PrimeKey",
                sectionTitle = "PrimeKey",
                sectionDescription = "PrimeKey",
                sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
                sectionFields = listOf(
                    primeKey,
                    changeKind,
                    note
                ),
                sectionSortOrder = emptyList(),
                includedChanges = setOf(ChangeKind.MODIFIED)
            )

            val thrown = catchThrowable {
                executeChangeDetectionTest(
                    baselineRecordsValues = "PK VAL",
                    currentRecordsValues = "PK VAL_B",
                    expectedResultsValues = "::PK MODIFIED",
                    sectionOutline = sectionOutlineWithModifiedChangeKind,
                    recordValueMapper = { values ->
                        mapOf(
                            primeKey to values[0]
                        )
                    },
                    changeRecordMapper = { it.toKeyFieldsAndIdFallbackAndChangeKindString() }
                )
            }

            assertThat(thrown).hasMessage("CorrelationPolicyByKeyAndAtomValues does not support operation: correlatingRecordPairs()")
            assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `change detection should fail with error when ChangeKind DUPLICATE_KEY_ALERT is used`() {

            val sectionOutlineWithDupeAlertChangeKind = SectionOutline(
                sectionShortTitle = "PrimeKey",
                sectionTitle = "PrimeKey",
                sectionDescription = "PrimeKey",
                sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
                sectionFields = listOf(
                    primeKey,
                    changeKind,
                    note
                ),
                sectionSortOrder = emptyList(),
                includedChanges = setOf(ChangeKind.DUPLICATE_KEY_ALERT)
            )

            val thrown = catchThrowable {
                executeChangeDetectionTest(
                    baselineRecordsValues = "PK VAL",
                    currentRecordsValues = "PK VAL_B",
                    expectedResultsValues = "::PK MODIFIED",
                    sectionOutline = sectionOutlineWithDupeAlertChangeKind,
                    recordValueMapper = { values ->
                        mapOf(
                            primeKey to values[0]
                        )
                    },
                    changeRecordMapper = { it.toKeyFieldsAndIdFallbackAndChangeKindString() }
                )
            }

            assertThat(thrown).hasMessage("CorrelationPolicyByKeyAndAtomValues does not support operation: duplicateCorrelationKeyRecords()")
            assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        }
    }

    @Nested
    inner class PrimeKeyWithAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "PrimeKeyWithAtom",
            sectionTitle = "PrimeKeyWithAtom",
            sectionDescription = "PrimeKeyWithAtom",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                primeKey,
                atom,
                changeKind,
                note,
                idFallbackField
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.additionAndDeletion()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "PK VAL #1, " +
                ", " +
                "::PK (#1) DELETED",

            "Single record addition, " +
                ", " +
                "PK VAL #1, " +
                "::PK (#1) ADDED",

            "Atom value changes in single primary key record, " +
                "PK VAL #1, " +
                "PK VAL_B #2, " +
                "::PK (#2) ADDED | ::PK (#1) DELETED",

            "Atom value changes in multiple primary key records, " +
                "PK VAL #1 | PK_B VAL #2, " +
                "PK VAL_B #3 | PK_B VAL_B #4, " +
                "::PK (#3) ADDED | ::PK_B (#4) ADDED | ::PK (#1) DELETED | ::PK_B (#2) DELETED"
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
                        atom to values[1],
                        idFallbackField to values[2]
                    )
                },
                changeRecordMapper = { it.toKeyFieldsAndIdFallbackAndChangeKindString() }
            )
        }
    }

    @Nested
    inner class ContextParentAndPrimeKey {
        private val sectionOutline = SectionOutline(
            sectionShortTitle = "ContextParentAndPrimeKey",
            sectionTitle = "ContextParentAndPrimeKey",
            sectionDescription = "ContextParentAndPrimeKey",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                contextParentKey,
                primeKey,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.additionAndDeletion()
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
                "CTX PK | CTX PK_B | CTX PK_C, " +
                ", " +
                "CTX::PK DELETED | CTX::PK_B DELETED | CTX::PK_C DELETED",

            "Multiple record addition, " +
                ", " +
                "CTX PK | CTX PK_B | CTX PK_C, " +
                "CTX::PK ADDED | CTX::PK_B ADDED | CTX::PK_C ADDED",

            "ContextParent isolates equal prime keys, " +
                "CTX PK | CTX_B PK | CTX_C PK, " +
                "CTX PK | CTX_C PK, " +
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
                changeRecordMapper = { it.toKeyFieldsAndChangeKindString() }
            )
        }
    }

    @Nested
    inner class ContextParentAndPrimeKeyWithAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "ContextParentAndPrimeKeyWithAtom",
            sectionTitle = "ContextParentAndPrimeKeyWithAtom",
            sectionDescription = "ContextParentAndPrimeKeyWithAtom",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                contextParentKey,
                primeKey,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.additionAndDeletion()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "CTX PK VAL #1, " +
                ", " +
                "CTX::PK (#1) DELETED",

            "Single record addition, " +
                ", " +
                "CTX PK VAL #1, " +
                "CTX::PK (#1) ADDED",

            "Atom value changes in single ctx + primary key record, " +
                "CTX PK VAL #1, " +
                "CTX PK VAL_B #2, " +
                "CTX::PK (#2) ADDED | CTX::PK (#1) DELETED",

            "Atom value changes in multiple ctx + primary key records, " +
                "CTX PK VAL #1 | CTX PK_B VAL #2, " +
                "CTX PK VAL_B #3 | CTX PK_B VAL_B #4, " +
                "CTX::PK (#3) ADDED | CTX::PK_B (#4) ADDED | CTX::PK (#1) DELETED | CTX::PK_B (#2) DELETED",

            "ContextParent isolates equal prime keys, " +
                "CTX PK VAL #1 | CTX_B PK VAL #2 | CTX_C PK VAL #3, " +
                "CTX PK VAL #4 | CTX_C PK VAL_B #5, " +
                "CTX_C::PK (#5) ADDED | CTX_B::PK (#2) DELETED | CTX_C::PK (#3) DELETED"
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
                        atom to values[2],
                        idFallbackField to values[3]
                    )
                },
                changeRecordMapper = { it.toKeyFieldsAndIdFallbackAndChangeKindString() }
            )
        }
    }

    @Nested
    inner class NormalParentAndPrimeKey {
        private val sectionOutline = SectionOutline(
            sectionShortTitle = "NormalParentAndPrimeKey",
            sectionTitle = "NormalParentAndPrimeKey",
            sectionDescription = "NormalParentAndPrimeKey",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                parentKey,
                primeKey,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.additionAndDeletion()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "PAR PK #1, " +
                ", " +
                ":PAR:PK (#1) DELETED",

            "Single record addition, " +
                ", " +
                "PAR PK #1, " +
                ":PAR:PK (#1) ADDED",

            "Multiple record deletion, " +
                "PAR PK #1 | PAR PK_B #2 | PAR PK_C #3, " +
                ", " +
                ":PAR:PK (#1) DELETED | :PAR:PK_B (#2) DELETED | :PAR:PK_C (#3) DELETED",

            "Multiple record addition, " +
                ", " +
                "PAR PK #1 | PAR PK_B #2 | PAR PK_C #3, " +
                ":PAR:PK (#1) ADDED | :PAR:PK_B (#2) ADDED | :PAR:PK_C (#3) ADDED",

            "Parent isolates equal prime keys, " +
                "PAR PK #1 | PAR_B PK #2 | PAR_C PK #3, " +
                "PAR PK #4 | PAR_C PK #5, " +
                ":PAR_B:PK (#2) DELETED"
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
                        idFallbackField to values[2]
                    )
                },
                changeRecordMapper = { it.toKeyFieldsAndIdFallbackAndChangeKindString() }
            )
        }
    }

    @Nested
    inner class NormalParentAndPrimeKeyWithAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "NormalParentAndPrimeKeyWithAtom",
            sectionTitle = "NormalParentAndPrimeKeyWithAtom",
            sectionDescription = "NormalParentAndPrimeKeyWithAtom",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                parentKey,
                primeKey,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.additionAndDeletion()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "PAR PK VAL #1, " +
                ", " +
                ":PAR:PK (#1) DELETED",

            "Single record addition, " +
                ", " +
                "PAR PK VAL #1, " +
                ":PAR:PK (#1) ADDED",

            "Atom value changes in single parent + primary key record, " +
                "PAR PK VAL #1, " +
                "PAR PK VAL_B #2, " +
                ":PAR:PK (#2) ADDED | :PAR:PK (#1) DELETED",

            "Atom value changes in multiple parent + primary key records, " +
                "PAR PK VAL #1 | PAR PK_B VAL #2, " +
                "PAR PK VAL_B #3 | PAR PK_B VAL_B #4, " +
                ":PAR:PK (#3) ADDED | :PAR:PK_B (#4) ADDED | :PAR:PK (#1) DELETED | :PAR:PK_B (#2) DELETED",

            "Parent isolates equal prime keys, " +
                "PAR PK VAL #1 | PAR_B PK VAL #2 | PAR_C PK VAL #3, " +
                "PAR PK VAL #4 | PAR_C PK VAL_B #5, " +
                ":PAR_C:PK (#5) ADDED | :PAR_B:PK (#2) DELETED | :PAR_C:PK (#3) DELETED"
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
                        atom to values[2],
                        idFallbackField to values[3]
                    )
                },
                changeRecordMapper = { it.toKeyFieldsAndIdFallbackAndChangeKindString() }
            )
        }
    }

    @Nested
    inner class ContextParentNormalParentAndPrimeKey {
        private val sectionOutline = SectionOutline(
            sectionShortTitle = "ContextParentNormalParentAndPrimeKey",
            sectionTitle = "ContextParentNormalParentAndPrimeKey",
            sectionDescription = "ContextParentNormalParentAndPrimeKey",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                contextParentKey,
                parentKey,
                primeKey,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.additionAndDeletion()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "CTX PAR PK #1, " +
                ", " +
                "CTX:PAR:PK (#1) DELETED",

            "Single record addition, " +
                ", " +
                "CTX PAR PK #1, " +
                "CTX:PAR:PK (#1) ADDED",

            "Multiple record deletion, " +
                "CTX PAR PK #1 | CTX PAR PK_B #2 | CTX PAR PK_C #3, " +
                ", " +
                "CTX:PAR:PK (#1) DELETED | CTX:PAR:PK_B (#2) DELETED | CTX:PAR:PK_C (#3) DELETED",

            "Multiple record addition, " +
                ", " +
                "CTX PAR PK #1 | CTX PAR PK_B #2| CTX PAR PK_C #3, " +
                "CTX:PAR:PK (#1) ADDED | CTX:PAR:PK_B (#2) ADDED | CTX:PAR:PK_C (#3) ADDED",

            "ContextParent isolates equal prime keys, " +
                "CTX PAR PK #1 | CTX_B PAR PK #2 | CTX_C PAR PK #3, " +
                "CTX PAR PK #4 | CTX_C PAR PK #5, " +
                "CTX_B:PAR:PK (#2) DELETED"
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
                        idFallbackField to values[3]
                    )
                },
                changeRecordMapper = { it.toKeyFieldsAndIdFallbackAndChangeKindString() }
            )
        }
    }

    @Nested
    inner class ContextParentNormalParentAndPrimeKeyWithAtom {

        private val sectionOutline = SectionOutline(
            sectionShortTitle = "ContextParentNormalParentAndPrimeKeyWithAtom",
            sectionTitle = "ContextParentNormalParentAndPrimeKeyWithAtom",
            sectionDescription = "ContextParentNormalParentAndPrimeKeyWithAtom",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                contextParentKey,
                parentKey,
                primeKey,
                atom,
                changeKind,
                note
            ),
            sectionSortOrder = emptyList(),
            includedChanges = ChangeKind.additionAndDeletion()
        )

        @ParameterizedTest(name = "{0}")
        @CsvSource(
            "Single record deletion, " +
                "CTX PAR PK VAL #1, " +
                ", " +
                "CTX:PAR:PK (#1) DELETED",

            "Single record addition, " +
                ", " +
                "CTX PAR PK VAL #1, " +
                "CTX:PAR:PK (#1) ADDED",

            "Atom value changes in single ctx + parent + primary key record, " +
                "CTX PAR PK VAL #1, " +
                "CTX PAR PK VAL_B #2, " +
                "CTX:PAR:PK (#2) ADDED | CTX:PAR:PK (#1) DELETED",

            "Atom value changes in multiple ctx + parent + primary key records, " +
                "CTX PAR PK VAL #1 | CTX PAR PK_B VAL #2, " +
                "CTX PAR PK VAL_B #3 | CTX PAR PK_B VAL_B #4, " +
                "CTX:PAR:PK (#3) ADDED | CTX:PAR:PK_B (#4) ADDED | CTX:PAR:PK (#1) DELETED | CTX:PAR:PK_B (#2) DELETED",

            "ContextParent isolates equal prime keys, " +
                "CTX PAR PK VAL #1 | CTX_B PAR PK VAL #2 | CTX_C PAR PK VAL #3, " +
                "CTX PAR PK VAL #4 | CTX_C PAR PK VAL_B #5, " +
                "CTX_C:PAR:PK (#5) ADDED | CTX_B:PAR:PK (#2) DELETED | CTX_C:PAR:PK (#3) DELETED"
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
                        atom to values[3],
                        idFallbackField to values[4]
                    )
                },
                changeRecordMapper = { it.toKeyFieldsAndIdFallbackAndChangeKindString() }
            )
        }
    }
}
