package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.ValidationResults

object ValidateAndTransformReportSectionOption {

    private val optName = OptName.REPORT_SECTIONS

    fun includedReportSections(
        reportSections: String?,
        validationResults: ValidationResults
    ): List<String>? {

        return if (reportSections == null) {
            null
        } else {
            doCheckReportSections(reportSections, optName, validationResults)
        }
    }

    private fun doCheckReportSections(
        reportSections: String,
        optName: OptName,
        validationResults: ValidationResults
    ): List<String> {

        if (reportSections.isEmpty()) {
            validationResults.add(optName.nameString, "is empty")
            return emptyList()
        }

        val sections = reportSections.split(",").map { it.trim() }

        return when {

            sections.any { language -> language.isBlank() } -> {
                validationResults.add(optName.nameString, "has blank report section name")
                emptyList()
            }

            sections.groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
                .isNotEmpty()
            -> {
                validationResults.add(optName.nameString, "has duplicate report section names")
                emptyList()
            }
            else -> {

                return sections
            }
        }
    }
}
