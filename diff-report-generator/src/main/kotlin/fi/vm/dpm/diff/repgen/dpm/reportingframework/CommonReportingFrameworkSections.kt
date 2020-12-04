package fi.vm.dpm.diff.repgen.dpm.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.dpm.utils.DpmElementQueryDescriptor
import fi.vm.dpm.diff.repgen.dpm.utils.DpmOverviewSectionPlanComposer
import fi.vm.dpm.diff.repgen.dpm.utils.DpmTranslationSectionPlanComposer
import fi.vm.dpm.diff.repgen.dpm.utils.TranslationLangsOptionHelper

object CommonReportingFrameworkSections {

    private val reportingFrameworkElementQueryDescriptors = listOf(
        DpmElementQueryDescriptor(
            elementType = "ReportingFramework",
            elementTableName = "mReportingFramework",
            elementIdColumn = "FrameworkID",
            elementCodeColumn = "FrameworkCode",
            elementInherentLabelColumn = "FrameworkLabel",
            parentType = "",
            parentCodeStatement = "NULL",
            parentTableJoin = "",
            elementTableSliceCriteria = ""
        ),

        DpmElementQueryDescriptor(
            elementType = "Taxonomy",
            elementTableName = "mTaxonomy",
            elementIdColumn = "TaxonomyID",
            elementCodeColumn = "TaxonomyCode",
            elementInherentLabelColumn = "TaxonomyLabel",
            parentType = "ReportingFramework",
            parentCodeStatement = "mReportingFramework.FrameworkCode",
            parentTableJoin = "LEFT JOIN mReportingFramework ON mReportingFramework.FrameworkID = mTaxonomy.FrameworkID",
            elementTableSliceCriteria = ""
        ),

        DpmElementQueryDescriptor(
            elementType = "Module",
            elementTableName = "mModule",
            elementIdColumn = "ModuleID",
            elementCodeColumn = "ModuleCode",
            elementInherentLabelColumn = "ModuleLabel",
            parentType = "Taxonomy",
            parentCodeStatement = "mTaxonomy.TaxonomyCode",
            parentTableJoin = "LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mModule.TaxonomyID",
            elementTableSliceCriteria = ""
        ),

        DpmElementQueryDescriptor(
            elementType = "Table",
            elementTableName = "mTable",
            elementIdColumn = "TableID",
            elementCodeColumn = "TableCode",
            elementInherentLabelColumn = "TableLabel",
            parentType = "Taxonomy",
            parentCodeStatement = "mTaxonomy.TaxonomyCode",
            parentTableJoin = """
                LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
                LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
                """.trimLineStartsAndConsequentBlankLines(),
            elementTableSliceCriteria = ""
        )
    )

    fun overviewSectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val planComposer = DpmOverviewSectionPlanComposer(
            reportingFrameworkElementQueryDescriptors,
            dpmSectionOptions
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = planComposer.sectionOutline(
                sectionShortTitle = "RepFwOverview",
                sectionTitle = "ReportingFramework overview",
                sectionDescription = "Added and deleted Reporting Frameworks, Taxonomies, Modules and Tables"
            ),

            queryColumnMapping = planComposer.queryColumnMapping(),

            query = planComposer.query(),

            sourceTableDescriptors = listOf(
                "mReportingFramework",
                "mTaxonomy",
                "mModule",
                "mTable"
            )
        )
    }

    fun translationSectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val planComposer = DpmTranslationSectionPlanComposer(
            reportingFrameworkElementQueryDescriptors,
            dpmSectionOptions
        )

        val translationLangsOptionHelper = TranslationLangsOptionHelper(dpmSectionOptions)

        return SectionPlanSql.withSingleQuery(
            sectionOutline = planComposer.sectionOutline(
                sectionShortTitle = "RepFwTranslation",
                sectionTitle = "ReportingFramework translations",
                sectionDescription = "Label and description changes in Reporting Frameworks, Taxonomies, Modules and Tables"
            ),

            queryColumnMapping = planComposer.queryColumnMapping(),

            query = planComposer.query(),

            sourceTableDescriptors = listOf(
                translationLangsOptionHelper.sourceTableDescriptor(
                    elementTable = "mReportingFramework",
                    conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mReportingFramework.ConceptID"
                ),

                translationLangsOptionHelper.sourceTableDescriptor(
                    elementTable = "mTaxonomy",
                    conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mTaxonomy.ConceptID"
                ),

                translationLangsOptionHelper.sourceTableDescriptor(
                    elementTable = "mTable",
                    conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mTable.ConceptID"
                ),

                translationLangsOptionHelper.sourceTableDescriptor(
                    elementTable = "mModule",
                    conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mModule.ConceptID"
                )
            )
        )
    }
}
