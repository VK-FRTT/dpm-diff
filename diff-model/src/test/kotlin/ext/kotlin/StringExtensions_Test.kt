package fi.vm.dpm.diff.model

import ext.kotlin.splitCamelCaseWords
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class StringExtensions_Test {

    @DisplayName("String.splitCamelCaseWords")
    @ParameterizedTest(name = "´{0}´ to ´{1}´")
    @CsvSource(
        "camelCase, camel Case",
        "TitleCase, Title Case",
        "ACRONYMAtStartCase, ACRONYM At Start Case",
        "middleACRONYMCase, middle ACRONYM Case",
        "endCaseACRONYM, end Case ACRONYM",
        "phraseWith MiddleSpace, phrase With Middle Space",
        "parentheses (withContent), parentheses (with Content)"
    )
    fun testSplitCamelCaseWords(
        input: String,
        expected: String
    ) {
        assertThat(input.splitCamelCaseWords()).isEqualTo(expected)
    }
}
