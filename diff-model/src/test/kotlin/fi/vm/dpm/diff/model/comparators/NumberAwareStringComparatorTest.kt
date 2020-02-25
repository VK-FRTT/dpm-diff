package fi.vm.dpm.diff.model.comparators

import fi.vm.dpm.diff.model.thisShouldNeverHappen
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class NumberAwareStringComparatorTest {

    @DisplayName("Compare")
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
        "aa10bb20, is-before, aa10bb30",
        "aa10bb20cc30, is-before, aa10bb20cc40",
        "aa10bb20cc30, is-equal, aa10bb20cc30",

        "aa10bb20, is-after, aa10bb",
        "aa10bb20cc30, is-after, aa10bb20cc"
    )
    fun testNumberAwareStringComparator(
        o1: String,
        expected: String,
        o2: String
    ) {
        val expectedResultVal = when (expected) {
            "is-after" -> 1
            "is-before" -> -1
            "is-equal" -> 0
            else -> thisShouldNeverHappen("Unsupported result: $expected")
        }

        val result = NumberAwareStringComparator().compare(o1, o2)
        assertThat(result).isEqualTo(expectedResultVal)
    }
}
