package fi.vm.dpm.diff.cli

data class CompareParamsDpm(
    val common: CompareParamsCommon,
    val identificationLabelLangCodes: List<String>,
    val translationLangCodes: List<String>?
)
