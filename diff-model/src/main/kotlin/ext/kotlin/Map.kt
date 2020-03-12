package ext.kotlin

import fi.vm.dpm.diff.model.Field

@Suppress("UNCHECKED_CAST")
inline fun <reified FK : Field, V : Any?> Map<out Field, V>.filterFieldType(): Map<FK, V> {
    val classCriteria = FK::class

    return filter { (key, _) ->
        (key::class == classCriteria)
    } as Map<FK, V>
}
