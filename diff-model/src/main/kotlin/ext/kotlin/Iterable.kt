package ext.kotlin

import fi.vm.dpm.diff.model.Field

@Suppress("UNCHECKED_CAST")
inline fun <reified FT : Field> Iterable<Field>.filterFieldType(): List<FT> {
    val classCriteria = FT::class

    return filter { it::class == classCriteria } as List<FT>
}
