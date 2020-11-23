package fi.vm.dpm.diff.repgen.section

import fi.vm.dpm.diff.repgen.SourceTableDescriptor
import fi.vm.dpm.diff.repgen.ext.kotlin.toQuotedAndCommaSeparatedString

object ElementTranslationHelpers {

    fun elementTranslationSourceTableDescriptor(
        elementTable: String,
        conceptTranslationJoin: String,
        translationLangCodes: List<String>?
    ): SourceTableDescriptor {

        return if (translationLangCodes == null) {
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
                where = "mLanguage.IsoCode IN (${translationLangCodes.toQuotedAndCommaSeparatedString()})"
            )
        }
    }

    fun translationLanguageWhereStatement(
        translationLangCodes: List<String>?
    ): String {
        return if (translationLangCodes != null) {
            """
            WHERE TranslationLanguage IN (
                ${translationLangCodes.toQuotedAndCommaSeparatedString()}
            )
            """
        } else {
            ""
        }
    }
}
