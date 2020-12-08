package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.model.thisShouldNeverHappen

class SectionPlanSql private constructor(
    private val sectionOutline: SectionOutline,
    private val queryColumnMapping: Map<String, Field>,
    private val query: String?,
    private val partitionedQueries: List<String>?,
    private val sourceTableDescriptors: List<Any>
) {
    companion object {

        fun withSingleQuery(
            sectionOutline: SectionOutline,
            queryColumnMapping: Map<String, Field>,
            query: String,
            sourceTableDescriptors: List<Any>
        ): SectionPlanSql {
            return SectionPlanSql(sectionOutline, queryColumnMapping, query, null, sourceTableDescriptors)
        }

        fun withPartitionedQueries(
            sectionOutline: SectionOutline,
            queryColumnMapping: Map<String, Field>,
            partitionedQueries: List<String>,
            sourceTableDescriptors: List<Any>
        ): SectionPlanSql {
            return SectionPlanSql(sectionOutline, queryColumnMapping, null, partitionedQueries, sourceTableDescriptors)
        }
    }

    fun sectionOutline() = sectionOutline

    fun queryColumnMapping() = queryColumnMapping

    fun partitionedQueries(): List<String> {
        return when {
            query != null -> listOf(query)
            partitionedQueries != null -> partitionedQueries
            else -> thisShouldNeverHappen("")
        }
    }

    fun sourceTableDescriptors() = sourceTableDescriptors

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
