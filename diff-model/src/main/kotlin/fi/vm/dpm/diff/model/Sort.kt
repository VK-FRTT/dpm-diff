package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.comparators.FixedOrderComparator
import fi.vm.dpm.diff.model.comparators.NullsFirstComparator
import fi.vm.dpm.diff.model.comparators.NumberAwareStringComparator

sealed class Sort(
    open val field: Field
) {
    abstract fun comparator(): Comparator<Any?>
}

data class NumberAwareSort(
    override val field: Field
) : Sort(field) {
    companion object {
        private val comparator = NullsFirstComparator(NumberAwareStringComparator())
    }

    override fun comparator(): Comparator<Any?> = comparator
}

data class FixedChangeKindSort(
    override val field: Field
) : Sort(field) {
    companion object {
        private val comparator = NullsFirstComparator(
            FixedOrderComparator(
                FixedOrderComparator.UnknownObjectMode.FAIL,
                ChangeKind.DELETED,
                ChangeKind.ADDED,
                ChangeKind.MODIFIED
            )
        )
    }

    override fun comparator(): Comparator<Any?> = comparator
}

data class FixedElementTypeSort(
    override val field: Field
) : Sort(field) {
    companion object {
        private val comparator = NullsFirstComparator(
            FixedOrderComparator(
                FixedOrderComparator.UnknownObjectMode.FAIL,
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
        )
    }

    override fun comparator(): Comparator<Any?> = comparator
}

data class FixedTranslationRoleSort(
    override val field: Field
) : Sort(field) {
    companion object {
        private val comparator = NullsFirstComparator(
            FixedOrderComparator(
                FixedOrderComparator.UnknownObjectMode.AFTER,
                "label",
                "description"
            )
        )
    }

    override fun comparator(): Comparator<Any?> = comparator
}
