package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.vkdata.section.DataPointIdSection

object VkDataSectionPlans {

    fun allPlans(): Collection<SectionPlanSql> {

        val vkDataSectionPlans = listOf(
            DataPointIdSection.sectionPlan()
        )

        return vkDataSectionPlans
    }
}
