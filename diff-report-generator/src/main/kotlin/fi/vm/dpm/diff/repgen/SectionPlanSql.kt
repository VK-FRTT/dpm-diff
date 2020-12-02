package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.SectionOutline

data class SectionPlanSql(
    val sectionOutline: SectionOutline,
    val queryColumnMapping: Map<String, Field>,
    val query: String,
    val sourceTableDescriptors: List<Any>
)
