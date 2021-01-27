package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.SourceDbs
import fi.vm.dpm.diff.repgen.vkdata.section.DataPointIdSection

object VkDataSectionPlans {

    fun allPlans(sourceDbs: SourceDbs): Collection<SectionPlanSql> {

        val vkDataSectionPlans = listOf(
            DataPointIdSection.sectionPlan(sourceDbs)
        )

        return vkDataSectionPlans
    }
}
