package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.model.diagnostic.ValidationResults
import fi.vm.dpm.diff.repgen.dpm.utils.SourceTableDescriptor

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

    fun validate(validationResults: ValidationResults) {

        validationResults.withSubject("SectionPlanSql.queryColumnMapping") {
            validateThat(
                queryColumnMapping.isNotEmpty(),
                "is empty"
            )

            queryColumnMapping
                .values
                .forEach { field ->
                    validateThat(
                        sectionOutline.sectionFields.contains(field),
                        "has unknown field",
                        field.fieldName
                    )
                }

            queryColumnMapping
                .values
                .groupBy { it }
                .forEach { (_, mappingsHavingSameField) ->
                    validateThat(
                        mappingsHavingSameField.size == 1,
                        "has duplicate field",
                        mappingsHavingSameField.first().fieldName
                    )
                }
        }

        validationResults.withSubject("SectionPlanSql.partitionedQueries") {
            validateThat(
                partitionedQueries.isNotEmpty(),
                "is empty"
            )
        }

        validationResults.withSubject("SectionPlanSql.sourceTableDescriptors") {
            validateThat(
                sourceTableDescriptors.isNotEmpty(),
                "is empty"
            )

            sourceTableDescriptors.forEach {
                validateThat(
                    it is String || it is SourceTableDescriptor,
                    "has unsupported descriptor type"
                )
            }
        }
    }
}
