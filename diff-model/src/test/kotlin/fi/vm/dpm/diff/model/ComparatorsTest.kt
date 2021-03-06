package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.comparators.FixedOrderComparator
import fi.vm.dpm.diff.model.comparators.NullsFirstComparator
import fi.vm.dpm.diff.model.comparators.NumberAwareStringComparator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ComparatorsTest {

    @DisplayName("FixedOrderComparator - UnknownObjectMode.AFTER")
    @ParameterizedTest(name = "´{0}´ {1} ´{2}´")
    @CsvSource(
        "Tuesday, is-after, Monday",
        "Monday, is-before, Tuesday",
        "Monday, is-before, Wednesday",
        "Monday, is-equal, Monday",

        "Foo, is-after, Monday",
        "Tuesday, is-before, Foo",
        "Foo, is-equal, Foo"
    )
    fun testFixedOrderComparator_unknownObjectModeAfter(
        o1: String,
        expected: String,
        o2: String
    ) {
        testComparator(
            o1 = o1,
            expected = expected,
            o2 = o2,
            comparator = FixedOrderComparator(
                unknownObjectMode = FixedOrderComparator.UnknownObjectMode.AFTER_WITH_NESTED_COMPARE,
                nestedComparator = NumberAwareStringComparator(),
                ordering = listOf(
                    "Monday",
                    "Tuesday",
                    "Wednesday"
                )
            )
        )
    }

    @DisplayName("FixedOrderComparator - UnknownObjectMode.FAIL")
    @ParameterizedTest(name = "´{0}´ {1} ´{2}´")
    @CsvSource(
        "Tuesday, is-after, Monday",
        "Monday, is-before, Tuesday",
        "Monday, is-before, Wednesday",
        "Monday, is-equal, Monday",

        "Foo, throws-exception, Monday",
        "Tuesday, throws-exception, Foo",
        "Foo, throws-exception, Foo"
    )
    fun testFixedOrderComparator_unknownObjectModeFail(
        o1: String,
        expected: String,
        o2: String
    ) {
        testComparator(
            o1 = o1,
            expected = expected,
            o2 = o2,
            comparator = FixedOrderComparator(
                unknownObjectMode = FixedOrderComparator.UnknownObjectMode.FAIL,
                ordering = listOf(
                    "Monday",
                    "Tuesday",
                    "Wednesday"
                )
            )
        )
    }

    @DisplayName("NullsFirstComparator")
    @ParameterizedTest(name = "´{0}´ {1} ´{2}´")
    @CsvSource(
        ", is-before, aa",
        "aa, is-after,",
        ", is-equal,",

        "aa, throws-exception, aa",
        "aa, throws-exception, bb"
    )
    fun testNullsFirstComparator(
        o1: String?,
        expected: String,
        o2: String?
    ) {
        class FailingComparator : Comparator<Any> {
            override fun compare(o1: Any, o2: Any): Int {
                throw Exception("Nested compare")
            }
        }

        testComparator(
            o1 = o1,
            expected = expected,
            o2 = o2,
            comparator = NullsFirstComparator(FailingComparator())
        )
    }

    @DisplayName("NumberAwareStringComparator")
    @ParameterizedTest(name = "´{0}´ {1} ´{2}´")
    @CsvSource(
        "bb, is-after, aa",
        "aa, is-before, bb",
        "bb, is-equal, bb",

        "1, is-after, 0",
        "2, is-after, 1",
        "1, is-before, 2",
        "2, is-equal, 2",

        "9, is-before, 10",
        "10, is-before, 11",
        "123, is-equal, 123",

        "aa10, is-before, aa11",
        "10aa, is-before, 11aa",
        "10aa, is-before, 10bb",
        "aa10bb20, is-before, aa10bb30",
        "aa10bb20cc30, is-before, aa10bb20cc40",
        "aa10bb20cc30, is-equal, aa10bb20cc30",

        "aa10bb20, is-after, aa10bb",
        "aa10bb20cc30, is-after, aa10bb20cc",

        "aa10, is-before, aa10aa",
        "aa10aa, is-after, aa10",
        "aa10, is-equal, aa10",

        "aa10aa, is-before, aa10bb",
        "aa10bb, is-after, aa10aa",
        "aa10aa, is-equal, aa10aa",

        "aa10, is-before, aa10bb20",
        "aa100, is-after, aa10bb20"
    )
    fun testNumberAwareStringComparator(
        o1: String,
        expected: String,
        o2: String
    ) {
        testComparator(
            o1 = o1,
            expected = expected,
            o2 = o2,
            comparator = NumberAwareStringComparator()
        )
    }

    private fun <T : Any?> testComparator(
        o1: T,
        expected: String,
        o2: T,
        comparator: Comparator<T>
    ) {
        if (expected == "throws-exception") {
            val throwable = catchThrowable {
                comparator.compare(o1, o2)
            }
            assertThat(throwable).isNotNull()
        } else {
            val result = comparator.compare(o1, o2)

            when (expected) {
                "is-after" -> {
                    assertThat(result).isGreaterThan(0)
                }
                "is-before" -> {
                    assertThat(result).isLessThan(0)
                }

                "is-equal" -> {
                    assertThat(result).isEqualTo(0)
                }
                else -> thisShouldNeverHappen("Unsupported expected: $expected")
            }
        }
    }
}
