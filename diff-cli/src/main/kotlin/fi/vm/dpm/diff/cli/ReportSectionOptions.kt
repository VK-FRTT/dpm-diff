package fi.vm.dpm.diff.cli

import fi.vm.dpm.diff.model.diagnostic.ValidationResults

object ReportSectionOptions {

    fun checkIncludedReportSections(
        reportSections: String?,
        optName: OptName,
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
        val sections = reportSections.split(",").map { it.trim() }

        if (sections.isEmpty()) {
            validationResults.add(optName.nameString, "is empty")
        }

        if (sections.any { language -> language.isBlank() }) {
            validationResults.add(optName.nameString, "has blank report section name")
        }

        if (sections
                .groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
                .isNotEmpty()
        ) {
            validationResults.add(optName.nameString, "has duplicate report section names")
        }

        return sections
    }
}
