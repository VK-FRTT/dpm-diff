package fi.vm.dpm.diff.repgen.dpm.utils

import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.ext.kotlin.toQuotedAndCommaSeparatedString

class TranslationLangsOptionHelper(
    private val dpmSectionOptions: DpmSectionOptions
) {
    fun translationLanguageWhereStatement(): String {
        return if (dpmSectionOptions.translationLangCodes != null) {
            """
            WHERE TranslationLanguage IN (
                ${dpmSectionOptions.translationLangCodes.toQuotedAndCommaSeparatedString()}
            )
            """
        } else {
            ""
        }
    }

    fun sourceTableDescriptor(
        elementTable: String,
        conceptTranslationJoin: String
    ): SourceTableDescriptor {

        return if (dpmSectionOptions.translationLangCodes == null) {
            SourceTableDescriptor(
                table = elementTable,
                joins = listOf(conceptTranslationJoin)
            )
        } else {
            SourceTableDescriptor(
                table = elementTable,
                joins = listOf(
                    conceptTranslationJoin,
                    "mLanguage on mLanguage.LanguageID = mConceptTranslation.LanguageID"
                ),
                where = "mLanguage.IsoCode IN (${dpmSectionOptions.translationLangCodes.toQuotedAndCommaSeparatedString()})"
            )
        }
    }
}
