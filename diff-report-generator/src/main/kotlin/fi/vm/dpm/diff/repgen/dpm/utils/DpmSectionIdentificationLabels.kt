package fi.vm.dpm.diff.repgen.dpm.utils

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.IdentificationLabelField
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions

class DpmSectionIdentificationLabels(
    val fieldNameBase: String,
    val dpmSectionOptions: DpmSectionOptions
) {
    val labelFields = dpmSectionOptions.identificationLabelLangCodes.map { langCode ->
        IdentificationLabelField(
            fieldName = "$fieldNameBase${langCode.toUpperCase()}"
        )
    }

    fun labelFields(): Array<IdentificationLabelField> {
        return labelFields.toTypedArray()
    }

    fun labelColumnMapping(): Array<Pair<String, Field>> {
        return dpmSectionOptions.identificationLabelLangCodes.mapIndexed { index, langCode ->
            val field = labelFields[index]
            val columnName = idLabelColumnName(langCode)
            Pair(columnName, field)
        }.toTypedArray()
    }

    fun labelColumnNamesFragment(): String {
        return dpmSectionOptions.identificationLabelLangCodes.map { langCode ->
            """
             ,${idLabelColumnName(langCode)} AS '${idLabelColumnName(langCode)}'
            """.trimLineStartsAndConsequentBlankLines()
        }.joinToString(
            separator = "\n"
        )
    }

    fun labelAggregateFragment(): String {
        return dpmSectionOptions.identificationLabelLangCodes.map { langCode ->
            """
            ,MAX(CASE WHEN mLanguage.IsoCode = '$langCode' THEN mConceptTranslation.Text END) AS ${idLabelColumnName(
                langCode
            )}
            """.trimLineStartsAndConsequentBlankLines()
        }.joinToString(separator = "\n")
    }

    fun idLabelColumnName(langCode: String): String {
        return "IdLabel${langCode.toUpperCase()}"
    }
}
