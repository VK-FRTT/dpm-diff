package fi.vm.dpm.diff.model.comparators

import fi.vm.dpm.diff.model.thisShouldNeverHappen

class FixedOrderComparator<T : Any>(
    private val unknownObjectMode: UnknownObjectMode,
    private vararg val ordering: T
) : Comparator<T> {

    enum class UnknownObjectMode {
        FAIL,
        AFTER,
    }

    private val orderLookup: Map<T, Int> by lazy {
        ordering.mapIndexed { index, item ->
            item to index
        }.toMap()
    }

    override fun compare(o1: T, o2: T): Int {
        val position1 = orderLookup[o1]
        val position2 = orderLookup[o2]

        if (unknownObjectMode == UnknownObjectMode.FAIL) {
            position1 ?: thisShouldNeverHappen("No position for object: $o1")
            position2 ?: thisShouldNeverHappen("No position for object: $o2")
        } else {
            if (position1 == null && position2 == null) {
                return 0
            }

            if (position1 == null) {
                return 1
            }

            if (position2 == null) {
                return -1
            }
        }
        return position1.compareTo(position2)
    }
}
