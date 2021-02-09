package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.model.comparators.FixedOrderComparator
import fi.vm.dpm.diff.model.comparators.NullsFirstComparator
import fi.vm.dpm.diff.model.comparators.NumberAwareStringComparator

sealed class SortBy(
    open val field: Field
) {
    abstract fun comparator(): Comparator<Any?>
}

data class NumberAwareSortBy(
    override val field: Field
) : SortBy(field) {
    companion object {
        private val comparator = NullsFirstComparator(NumberAwareStringComparator())
    }

    override fun comparator(): Comparator<Any?> = comparator
}

data class FixedChangeKindSortBy(
    override val field: Field
) : SortBy(field) {
    companion object {
        private val comparator = NullsFirstComparator(
            FixedOrderComparator(
                unknownObjectMode = FixedOrderComparator.UnknownObjectMode.FAIL,
                ordering = listOf(
                    ChangeKind.DELETED,
                    ChangeKind.ADDED,
                    ChangeKind.MODIFIED,
                    ChangeKind.DUPLICATE_KEY_ALERT
                )
            )
        )
    }

    override fun comparator(): Comparator<Any?> = comparator
}

data class FixedElementTypeSortBy(
    override val field: Field
) : SortBy(field) {
    companion object {
        private val comparator = NullsFirstComparator(
            FixedOrderComparator(
                unknownObjectMode = FixedOrderComparator.UnknownObjectMode.FAIL,
                ordering = listOf(
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
        )
    }

    override fun comparator(): Comparator<Any?> = comparator
}

data class FixedTranslationRoleSortBy(
    override val field: Field
) : SortBy(field) {
    companion object {
        private val comparator = NullsFirstComparator(
            FixedOrderComparator(
                unknownObjectMode = FixedOrderComparator.UnknownObjectMode.AFTER_WITH_NESTED_COMPARE,
                nestedComparator = NumberAwareStringComparator(),
                ordering = listOf(
                    "label",
                    "description"
                )
            )
        )
    }

    override fun comparator(): Comparator<Any?> = comparator
}
