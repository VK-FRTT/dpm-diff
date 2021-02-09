package fi.vm.dpm.diff.model

import java.util.Comparator

class ChangeRecordComparator(
    private val sectionSortOrder: List<SortBy>
) : Comparator<ChangeRecord> {

    override fun compare(record1: ChangeRecord, record2: ChangeRecord): Int {
        sectionSortOrder.forEach { sort ->

            val value1 = record1.fields[sort.field]
            val value2 = record2.fields[sort.field]

            val result = sort.comparator().compare(value1, value2)

            if (result != 0) {
                return result
            }
        }
        return 0
    }
}
