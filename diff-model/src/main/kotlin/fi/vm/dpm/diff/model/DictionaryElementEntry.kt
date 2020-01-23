package fi.vm.dpm.diff.model

data class DictionaryElementEntry(
    val elementCode: String,
    val elementType: DictionaryElementType,
    val labels: Translations
)
