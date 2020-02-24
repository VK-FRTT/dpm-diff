package ext.kotlin

import fi.vm.dpm.diff.model.Field

@Suppress("UNCHECKED_CAST")
inline fun <T : Field, reified FT : Field> Iterable<T>.filterFieldType(): List<FT> {
    val classCriteria = FT::class

    return filter { it::class == classCriteria } as List<FT>
}
