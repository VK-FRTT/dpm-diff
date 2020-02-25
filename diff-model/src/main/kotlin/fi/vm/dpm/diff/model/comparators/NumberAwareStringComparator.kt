package fi.vm.dpm.diff.model.comparators

import java.math.BigInteger
import java.util.Comparator

class NumberAwareStringComparator : Comparator<Any> {
    private val regex = """(\D*)(\d+)""".toRegex()

    override fun compare(string1: Any, string2: Any): Int {
        string1 as String
        string2 as String

        val matchIterator1 = regex.findAll(string1).iterator()
        val matchIterator2 = regex.findAll(string2).iterator()

        if (!matchIterator1.hasNext() || !matchIterator2.hasNext()) {
            return string1.compareTo(string2)
        }

        while (matchIterator1.hasNext() && matchIterator2.hasNext()) {
            val compareResult = compareMatchResults(
                matchIterator1.next(),
                matchIterator2.next()
            )
            if (compareResult != 0) return compareResult
        }

        return compareIteratorTails(
            matchIterator1,
            matchIterator2
        )
    }

    private fun compareMatchResults(
        matchResult1: MatchResult,
        matchResult2: MatchResult
    ): Int {
        val (nondigits1, digits1) = matchResult1.destructured
        val (nondigits2, digits2) = matchResult2.destructured

        val nonDigitResult = nondigits1.compareTo(nondigits2)
        if (nonDigitResult != 0) return nonDigitResult
        val number1 = BigInteger(digits1)
        val number2 = BigInteger(digits2)
        return number1.compareTo(number2)
    }

    private fun compareIteratorTails(
        matchIterator1: Iterator<MatchResult>,
        matchIterator2: Iterator<MatchResult>
    ): Int {
        return if (!matchIterator1.hasNext() && !matchIterator2.hasNext()) {
            0
        } else if (!matchIterator1.hasNext()) {
            -1
        } else {
            1
        }
    }
}
