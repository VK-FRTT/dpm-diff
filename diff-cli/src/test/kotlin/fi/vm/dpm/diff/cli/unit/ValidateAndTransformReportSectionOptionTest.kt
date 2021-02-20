package fi.vm.dpm.diff.cli.unit

import fi.vm.dpm.diff.cli.ValidateAndTransformReportSectionOption
import fi.vm.dpm.diff.model.diagnostic.ValidationResults
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ValidateAndTransformReportSectionOptionTest {

    private lateinit var validationResults: ValidationResults

    @BeforeEach
    fun testInit() {
        validationResults = ValidationResults()
    }

    @Test
    fun `Should tokenize comma separated list of sections`() {
        val reportSections = ValidateAndTransformReportSectionOption.includedReportSections(
            "domain,member, metric, dimension",
            validationResults
        )

        assertThat(validationResults.messages()).isEmpty()
        assertThat(reportSections).containsExactly(
            "domain",
            "member",
            "metric",
            "dimension"
        )
    }

    @Test
    fun `Should return null report sections when input is null`() {
        val reportSections = ValidateAndTransformReportSectionOption.includedReportSections(
            null,
            validationResults
        )

        assertThat(reportSections).isNull()
    }

    @Test
    fun `Should provide validation message when input is empty`() {
        val reportSections = ValidateAndTransformReportSectionOption.includedReportSections(
            "",
            validationResults
        )

        assertThat(validationResults.messages()).containsExactly(
            "reportSections: is empty"
        )

        assertThat(reportSections).isEmpty()
    }

    @Test
    fun `Should provide validation message when input has empty section`() {
        val reportSections = ValidateAndTransformReportSectionOption.includedReportSections(
            "domain,member, , dimension",
            validationResults
        )

        assertThat(validationResults.messages()).containsExactly(
            "reportSections: has blank report section name"
        )

        assertThat(reportSections).isEmpty()
    }

    @Test
    fun `Should provide validation message when input has single section more than once`() {
        val reportSections = ValidateAndTransformReportSectionOption.includedReportSections(
            "domain,member, member, dimension",
            validationResults
        )

        assertThat(validationResults.messages()).containsExactly(
            "reportSections: has duplicate report section names"
        )

        assertThat(reportSections).isEmpty()
    }
}
