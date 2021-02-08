package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.model.thisShouldNeverHappen

const val MAX_ITEMS_PER_PARTITION = 500_000

class SectionPlanSql private constructor(
    val sectionOutline: SectionOutline,
    val queryColumnMapping: Map<String, Field>,
    val partitionedQueries: List<String>,
    val sourceTableDescriptors: List<Any>
) {
    companion object {

        fun withSingleQuery(
            sectionOutline: SectionOutline,
            queryColumnMapping: Map<String, Field>,
            query: String,
            sourceTableDescriptors: List<Any>
        ): SectionPlanSql {
            return SectionPlanSql(sectionOutline, queryColumnMapping, listOf(query), sourceTableDescriptors)
        }

        fun withPartitionedQueries(
            sectionOutline: SectionOutline,
            queryColumnMapping: Map<String, Field>,
            partitionedQueries: List<String>,
            sourceTableDescriptors: List<Any>
        ): SectionPlanSql {
            return SectionPlanSql(sectionOutline, queryColumnMapping, partitionedQueries, sourceTableDescriptors)
        }
    }

    fun sanityCheck() {

        // sectionOutline
        sectionOutline.sanityCheck()

        // queryColumnMapping
        check(queryColumnMapping.isNotEmpty())

        queryColumnMapping.forEach { mapping ->
            check(sectionOutline.sectionFields.contains(mapping.value))
        }

        queryColumnMapping
            .values
            .groupBy { it }
            .forEach { (_, mappingsHavingSameField) ->
                check(mappingsHavingSameField.size == 1)
            }
    }

    private fun check(value: Boolean) {
        if (!value) {
            thisShouldNeverHappen("SectionPlanSql SanityCheck failed for: ${sectionOutline.sectionTitle}")
        }
    }
}
