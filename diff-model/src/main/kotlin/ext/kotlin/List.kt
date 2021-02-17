package ext.kotlin

import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.thisShouldNeverHappen

inline fun <reified FT : Field> List<Field>.firstFieldOfTypeOrNull(): FT? {
    val classCriteria = FT::class

    val fields = filter { it::class == classCriteria }

    if (fields.isEmpty()) return null
    return fields.first() as FT
}

inline fun <reified FT : Field> List<Field>.firstFieldOfType(): FT {
    return firstFieldOfTypeOrNull() ?: thisShouldNeverHappen("No field with type: ${FT::class.simpleName}")
}
