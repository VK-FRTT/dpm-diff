package fi.vm.dpm.diff.model.comparators

import fi.vm.dpm.diff.model.thisShouldNeverHappen

class FixedOrderComparator<T : Any>(
    private vararg val ordering: T
) : Comparator<T> {

    private val orderLookup: Map<T, Int> by lazy {
        ordering.mapIndexed { index, item ->
            item to index
        }.toMap()
    }

    override fun compare(o1: T, o2: T): Int {
        val position1 = orderLookup[o1] ?: thisShouldNeverHappen("No position for object: $o1")
        val position2 = orderLookup[o2] ?: thisShouldNeverHappen("No position for object: $o2")

        return position1.compareTo(position2)
    }
}
