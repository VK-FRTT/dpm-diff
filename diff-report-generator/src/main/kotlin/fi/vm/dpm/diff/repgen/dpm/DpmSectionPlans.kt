package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.dpm.dictionary.CommonDictionarySections
import fi.vm.dpm.diff.repgen.dpm.dictionary.DimensionSection
import fi.vm.dpm.diff.repgen.dpm.dictionary.DomainSection
import fi.vm.dpm.diff.repgen.dpm.dictionary.HierarchyNodeSection
import fi.vm.dpm.diff.repgen.dpm.dictionary.HierarchyNodeStructureSection
import fi.vm.dpm.diff.repgen.dpm.dictionary.HierarchySection
import fi.vm.dpm.diff.repgen.dpm.dictionary.MemberSection
import fi.vm.dpm.diff.repgen.dpm.dictionary.MetricSection
import fi.vm.dpm.diff.repgen.dpm.reportingframework.AxisOrdinateSection
import fi.vm.dpm.diff.repgen.dpm.reportingframework.AxisOrdinateTranslationSection
import fi.vm.dpm.diff.repgen.dpm.reportingframework.CommonReportingFrameworkSections
import fi.vm.dpm.diff.repgen.dpm.reportingframework.OrdinateCategorisationSection
import fi.vm.dpm.diff.repgen.dpm.reportingframework.TableAxisSection
import fi.vm.dpm.diff.repgen.dpm.reportingframework.TableSection

object DpmSectionPlans {

    fun allPlans(
        dpmSectionOptions: DpmSectionOptions
    ): Collection<SectionPlanSql> {

        val dictionarySectionPlans = listOf(
            CommonDictionarySections.overviewSectionPlan(dpmSectionOptions),
            CommonDictionarySections.translationSectionPlan(dpmSectionOptions),
            DomainSection.sectionPlan(dpmSectionOptions),
            MemberSection.sectionPlan(dpmSectionOptions),
            MetricSection.sectionPlan(dpmSectionOptions),
            DimensionSection.sectionPlan(dpmSectionOptions),
            HierarchySection.sectionPlan(dpmSectionOptions),
            HierarchyNodeSection.sectionPlan(dpmSectionOptions),
            HierarchyNodeStructureSection.sectionPlan(dpmSectionOptions)
        )

        val reportingFrameworkSectionPlans = listOf(
            CommonReportingFrameworkSections.overviewSectionPlan(dpmSectionOptions),
            CommonReportingFrameworkSections.translationSectionPlan(dpmSectionOptions),
            TableSection.sectionPlan(dpmSectionOptions),
            TableAxisSection.sectionPlan(dpmSectionOptions),
            AxisOrdinateSection.sectionPlan(dpmSectionOptions),
            AxisOrdinateTranslationSection.sectionPlan(dpmSectionOptions),
            OrdinateCategorisationSection.sectionPlan(dpmSectionOptions)
        )

        return dictionarySectionPlans + reportingFrameworkSectionPlans
    }
}
