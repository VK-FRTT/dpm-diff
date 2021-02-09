package fi.vm.dpm.diff.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

internal class SortByTest {

    private val primeKeyField = KeyField(
        fieldName = "PrimeKey",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private val changeKindField = ChangeKindField()

    @Test
    fun `NumberAwareSortBy should order ChangeRecords by key field values`() {

        executeChangeRecordsSortAndExpectSuccess(
            sortOrder = listOf(
                NumberAwareSortBy(primeKeyField)
            ),
            changeRecords = listOf(
                changeRecordWithPrimeKey(null),
                changeRecordWithPrimeKey(""),
                changeRecordWithPrimeKey("10aa"),
                changeRecordWithPrimeKey("10bb"),
                changeRecordWithPrimeKey("aa"),
                changeRecordWithPrimeKey("bb"),
                changeRecordWithPrimeKey("11"),
                changeRecordWithPrimeKey("11aa"),
                changeRecordWithPrimeKey("11bb"),
                changeRecordWithPrimeKey("11bb"),
                changeRecordWithPrimeKey("11aa"),
                changeRecordWithPrimeKey("11"),
                changeRecordWithPrimeKey("bb"),
                changeRecordWithPrimeKey("aa"),
                changeRecordWithPrimeKey("10bb"),
                changeRecordWithPrimeKey("10aa"),
                changeRecordWithPrimeKey(""),
                changeRecordWithPrimeKey(null)
            ),
            verificationFields = listOf(
                primeKeyField
            )

        ) { sortResult ->

            assertThat(sortResult).containsExactly(
                "null",
                "null",
                "",
                "",
                "10aa",
                "10aa",
                "10bb",
                "10bb",
                "11",
                "11",
                "11aa",
                "11aa",
                "11bb",
                "11bb",
                "aa",
                "aa",
                "bb",
                "bb"
            )
        }
    }

    @Test
    fun `FixedChangeKindSortBy should order ChangeRecords by ChangeKind`() {

        executeChangeRecordsSortAndExpectSuccess(
            sortOrder = listOf(
                FixedChangeKindSortBy(changeKindField)
            ),
            changeRecords = listOf(
                changeRecordWithChangeKind(null),
                changeRecordWithChangeKind(ChangeKind.ADDED),
                changeRecordWithChangeKind(ChangeKind.DELETED),
                changeRecordWithChangeKind(ChangeKind.MODIFIED),
                changeRecordWithChangeKind(ChangeKind.DUPLICATE_KEY_ALERT),
                changeRecordWithChangeKind(ChangeKind.DUPLICATE_KEY_ALERT),
                changeRecordWithChangeKind(ChangeKind.MODIFIED),
                changeRecordWithChangeKind(ChangeKind.DELETED),
                changeRecordWithChangeKind(ChangeKind.ADDED),
                changeRecordWithChangeKind(null)
            ),
            verificationFields = listOf(
                changeKindField
            )
        ) { sortResult ->

            assertThat(sortResult).containsExactly(
                "null",
                "null",
                "DELETED",
                "DELETED",
                "ADDED",
                "ADDED",
                "MODIFIED",
                "MODIFIED",
                "DUPLICATE_KEY_ALERT",
                "DUPLICATE_KEY_ALERT"
            )
        }
    }

    @Test
    fun `FixedChangeKindSortBy should fail when ChangeKind has unknown value`() {

        executeChangeRecordsSortAndExpectException(
            sortOrder = listOf(
                FixedChangeKindSortBy(changeKindField)
            ),
            changeRecords = listOf(
                ChangeRecord(
                    fields = mapOf(changeKindField to ChangeKind.DELETED)
                ),

                ChangeRecord(
                    fields = mapOf(changeKindField to "not-a-change-kind")
                )
            ),
            exceptionMessage = "No position for object: not-a-change-kind"
        )
    }

    @Test
    fun `FixedElementTypeSortBy should order ChangeRecords by element type value in key field`() {

        executeChangeRecordsSortAndExpectSuccess(
            sortOrder = listOf(
                FixedElementTypeSortBy(primeKeyField)
            ),
            changeRecords = listOf(
                changeRecordWithPrimeKey("Dimension"),
                changeRecordWithPrimeKey("Domain"),
                changeRecordWithPrimeKey("Hierarchy"),
                changeRecordWithPrimeKey("Member"),
                changeRecordWithPrimeKey("Metric"),
                changeRecordWithPrimeKey("Module"),
                changeRecordWithPrimeKey("ReportingFramework"),
                changeRecordWithPrimeKey("Table"),
                changeRecordWithPrimeKey("Taxonomy"),
                changeRecordWithPrimeKey(""),
                changeRecordWithPrimeKey(null)
            ),
            verificationFields = listOf(
                primeKeyField
            )

        ) { sortResult ->

            assertThat(sortResult).containsExactly(
                "null",
                "",
                "Domain",
                "Member",
                "Metric",
                "Dimension",
                "Hierarchy",
                "ReportingFramework",
                "Taxonomy",
                "Module",
                "Table"
            )
        }
    }

    @Test
    fun `FixedElementTypeSortBy should fail when element type value is unknown`() {

        executeChangeRecordsSortAndExpectException(
            sortOrder = listOf(
                FixedElementTypeSortBy(primeKeyField)
            ),
            changeRecords = listOf(
                ChangeRecord(
                    fields = mapOf(primeKeyField to "Domain")
                ),

                ChangeRecord(
                    fields = mapOf(primeKeyField to "not-a-element-type")
                )
            ),
            exceptionMessage = "No position for object: not-a-element-type"
        )
    }

    @Test
    fun `FixedTranslationRoleSortBy should order ChangeRecords by translation role value in key field`() {

        executeChangeRecordsSortAndExpectSuccess(
            sortOrder = listOf(
                FixedTranslationRoleSortBy(primeKeyField)
            ),
            changeRecords = listOf(
                changeRecordWithPrimeKey(null),
                changeRecordWithPrimeKey(""),
                changeRecordWithPrimeKey("label"),
                changeRecordWithPrimeKey("description"),
                changeRecordWithPrimeKey("custom-role"),
                changeRecordWithPrimeKey("custom-role"),
                changeRecordWithPrimeKey("label"),
                changeRecordWithPrimeKey("description"),
                changeRecordWithPrimeKey(""),
                changeRecordWithPrimeKey(null)
            ),
            verificationFields = listOf(
                primeKeyField
            )

        ) { sortResult ->

            assertThat(sortResult).containsExactly(
                "null",
                "null",
                "label",
                "label",
                "description",
                "description",
                "",
                "",
                "custom-role",
                "custom-role"
            )
        }
    }

    @Test
    fun `ChangeRecordComparator should order ChangeRecords with multiple SortBy`() {

        executeChangeRecordsSortAndExpectSuccess(
            sortOrder = listOf(
                NumberAwareSortBy(primeKeyField),
                FixedChangeKindSortBy(changeKindField)
            ),
            changeRecords = listOf(
                changeRecordWithPrimeKeyAndChangeKind("11aa", ChangeKind.MODIFIED),
                changeRecordWithPrimeKeyAndChangeKind("11aa", ChangeKind.DELETED),
                changeRecordWithPrimeKeyAndChangeKind("10aa", ChangeKind.MODIFIED),
                changeRecordWithPrimeKeyAndChangeKind("10aa", ChangeKind.DELETED),

                changeRecordWithPrimeKeyAndChangeKind("10aa", ChangeKind.DELETED),
                changeRecordWithPrimeKeyAndChangeKind("10aa", ChangeKind.MODIFIED),
                changeRecordWithPrimeKeyAndChangeKind("11aa", ChangeKind.DELETED),
                changeRecordWithPrimeKeyAndChangeKind("11aa", ChangeKind.MODIFIED)
            ),
            verificationFields = listOf(
                primeKeyField,
                changeKindField
            )

        ) { sortResult ->

            assertThat(sortResult).containsExactly(
                "10aa, DELETED",
                "10aa, DELETED",
                "10aa, MODIFIED",
                "10aa, MODIFIED",
                "11aa, DELETED",
                "11aa, DELETED",
                "11aa, MODIFIED",
                "11aa, MODIFIED"
            )
        }
    }

    private fun changeRecordWithPrimeKey(key: String?) =
        ChangeRecord(
            fields = mapOf(primeKeyField to key)
        )

    private fun changeRecordWithChangeKind(changeKind: ChangeKind?) =
        ChangeRecord(
            fields = mapOf(changeKindField to changeKind)
        )

    private fun changeRecordWithPrimeKeyAndChangeKind(key: String?, changeKind: ChangeKind?) =
        ChangeRecord(
            fields = mapOf(primeKeyField to key, changeKindField to changeKind)
        )

    private fun executeChangeRecordsSortAndExpectSuccess(
        sortOrder: List<SortBy>,
        changeRecords: List<ChangeRecord>,
        verificationFields: List<Field>,
        verifyAction: (List<String>) -> Unit
    ) {
        val comparator = ChangeRecordComparator(sortOrder)

        val sortedChangeRecords = changeRecords.sortedWith(comparator)

        assertThat(changeRecords.size).isEqualTo(sortedChangeRecords.size)

        val sortResult = sortedChangeRecords.map { changeRecord ->
            changeRecord.fields
                .filterKeys { key -> verificationFields.contains(key) }
                .values
                .joinToString()
        }
        verifyAction(sortResult)
    }

    private fun executeChangeRecordsSortAndExpectException(
        sortOrder: List<SortBy>,
        changeRecords: List<ChangeRecord>,
        exceptionMessage: String
    ) {
        val comparator = ChangeRecordComparator(sortOrder)

        val thrown = catchThrowable {
            changeRecords.sortedWith(comparator)
        }

        assertThat(thrown)
            .hasMessage(exceptionMessage)
    }
}
