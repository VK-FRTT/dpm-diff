package fi.vm.dpm.diff.cli.unit

import fi.vm.dpm.diff.cli.ValidateAndTransformLangCodeOption
import fi.vm.dpm.diff.model.diagnostic.ValidationResults
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ValidateAndTransformLangCodeOptionTest {

    private lateinit var validationResults: ValidationResults

    @BeforeEach
    fun testInit() {

        validationResults = ValidationResults()
    }

    @Nested
    inner class IdentificationLabelLanguages {

        @Test
        fun `Should tokenize comma separated list of languages`() {
            val languages = ValidateAndTransformLangCodeOption.identificationLabelLanguages(
                identificationLabelLanguages = "fi,sv, en, pl",
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).isEmpty()

            assertThat(languages).containsExactly(
                "fi",
                "sv",
                "en",
                "pl"
            )
        }

        @Test
        fun `Should provide validation message when input is null`() {
            val languages = ValidateAndTransformLangCodeOption.identificationLabelLanguages(
                identificationLabelLanguages = null,
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "identificationLabelLanguages: missing required parameter value"
            )

            assertThat(languages).isEmpty()
        }

        @Test
        fun `Should provide validation message when input is empty`() {
            val languages = ValidateAndTransformLangCodeOption.identificationLabelLanguages(
                identificationLabelLanguages = "",
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "identificationLabelLanguages: is empty"
            )

            assertThat(languages).isEmpty()
        }

        @Test
        fun `Should provide validation message when input has empty language`() {
            val languages = ValidateAndTransformLangCodeOption.identificationLabelLanguages(
                identificationLabelLanguages = "fi,sv, , pl",
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "identificationLabelLanguages: has blank language code"
            )

            assertThat(languages).isEmpty()
        }

        @Test
        fun `Should provide validation message when input has single section more than once`() {
            val languages = ValidateAndTransformLangCodeOption.identificationLabelLanguages(
                identificationLabelLanguages = "fi,sv, sv, pl",
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "identificationLabelLanguages: has duplicate language codes"
            )

            assertThat(languages).isEmpty()
        }

        @Test
        fun `Should provide validation message when input has illegal characters`() {
            val languages = ValidateAndTransformLangCodeOption.identificationLabelLanguages(
                identificationLabelLanguages = "fi,sv, en#, pl",
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "identificationLabelLanguages: language code has illegal characters"
            )

            assertThat(languages).isEmpty()
        }
    }

    @Nested
    inner class TranslationLanguages {

        @Test
        fun `Should tokenize comma separated list of languages`() {
            val languages = ValidateAndTransformLangCodeOption.translationLanguages(
                translationLanguages = "fi,sv, en, pl",
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).isEmpty()

            assertThat(languages).containsExactly(
                "fi",
                "sv",
                "en",
                "pl"
            )
        }

        @Test
        fun `Should not provide validation message when input is null`() {
            val languages = ValidateAndTransformLangCodeOption.translationLanguages(
                translationLanguages = null,
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).isEmpty()

            assertThat(languages).isNull()
        }

        @Test
        fun `Should provide validation message when input is empty`() {
            val languages = ValidateAndTransformLangCodeOption.translationLanguages(
                translationLanguages = "",
                validationResults = validationResults
            )

            assertThat(validationResults.messages()).containsExactly(
                "translationLanguages: is empty"
            )

            assertThat(languages).isNull()
        }
    }
}
