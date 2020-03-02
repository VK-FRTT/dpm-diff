package fi.vm.dpm.diff.model.comparators

import java.util.Comparator

class NullsFirstComparator(
    private val nestedComparator: Comparator<Any>
) : Comparator<Any?> {

    override fun compare(o1: Any?, o2: Any?): Int {
        if (o1 == null && o2 == null) {
            return 0
        }

        if (o1 == null) {
            return -1
        }

        if (o2 == null) {
            return 1
        }

        return nestedComparator.compare(o1, o2)
    }
}
