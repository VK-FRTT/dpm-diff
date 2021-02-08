package fi.vm.dpm.diff.repgen.dpm.dictionary

import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.dpm.utils.DpmElementQueryDescriptor
import fi.vm.dpm.diff.repgen.dpm.utils.DpmOverviewSectionPlanComposer
import fi.vm.dpm.diff.repgen.dpm.utils.DpmTranslationSectionPlanComposer
import fi.vm.dpm.diff.repgen.dpm.utils.TranslationLangsOptionHelper

object CommonDictionarySections {

    private val dictionaryElementQueryDescriptors = listOf(
        DpmElementQueryDescriptor(
            elementType = "Domain",
            elementTableName = "mDomain",
            elementIdColumn = "DomainID",
            elementCodeColumn = "DomainCode",
            elementInherentLabelColumn = "DomainLabel",
            parentType = "",
            parentCodeStatement = "NULL",
            parentTableJoin = "",
            elementTableSliceCriteria = ""
        ),

        DpmElementQueryDescriptor(
            elementType = "Member",
            elementTableName = "mMember",
            elementIdColumn = "MemberID",
            elementCodeColumn = "MemberCode",
            elementInherentLabelColumn = "MemberLabel",
            parentType = "Domain",
            parentCodeStatement = "mDomain.DomainCode",
            parentTableJoin = "LEFT JOIN mDomain ON mDomain.DomainID = mMember.DomainID",
            elementTableSliceCriteria = "AND mMember.MemberID NOT IN (SELECT CorrespondingMemberID FROM mMetric)"
        ),

        DpmElementQueryDescriptor(
            elementType = "Metric",
            elementTableName = "mMember",
            elementIdColumn = "MemberID",
            elementCodeColumn = "MemberCode",
            elementInherentLabelColumn = "MemberLabel",
            parentType = "Domain",
            parentCodeStatement = "mDomain.DomainCode",
            parentTableJoin = "LEFT JOIN mDomain ON mDomain.DomainID = mMember.DomainID",
            elementTableSliceCriteria = "AND mMember.MemberID IN (SELECT CorrespondingMemberID FROM mMetric)"
        ),

        DpmElementQueryDescriptor(
            elementType = "Dimension",
            elementTableName = "mDimension",
            elementIdColumn = "DimensionID",
            elementCodeColumn = "DimensionCode",
            elementInherentLabelColumn = "DimensionLabel",
            parentType = "Domain",
            parentCodeStatement = "mDomain.DomainCode",
            parentTableJoin = "LEFT JOIN mDomain ON mDomain.DomainID = mDimension.DomainID",
            elementTableSliceCriteria = ""
        ),

        DpmElementQueryDescriptor(
            elementType = "Hierarchy",
            elementTableName = "mHierarchy",
            elementIdColumn = "HierarchyID",
            elementCodeColumn = "HierarchyCode",
            elementInherentLabelColumn = "HierarchyLabel",
            parentType = "Domain",
            parentCodeStatement = "mDomain.DomainCode",
            parentTableJoin = "LEFT JOIN mDomain on mDomain.DomainID = mHierarchy.DomainID",
            elementTableSliceCriteria = ""
        )
    )

    fun overviewSectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val planComposer = DpmOverviewSectionPlanComposer(
            dictionaryElementQueryDescriptors,
            dpmSectionOptions
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = planComposer.sectionOutline(
                sectionShortTitle = "DictOverview",
                sectionTitle = "Dictionary overview",
                sectionDescription = "Added and deleted Domains, Members, Metrics, Dimensions and Hierarchies"
            ),

            queryColumnMapping = planComposer.queryColumnMapping(),

            query = planComposer.query(),

            sourceTableDescriptors = listOf(
                "mDomain",
                "mMember",
                "mDimension",
                "mHierarchy"
            )
        )
    }

    fun translationSectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val planComposer = DpmTranslationSectionPlanComposer(
            dictionaryElementQueryDescriptors,
            dpmSectionOptions
        )

        val translationLangsOptionHelper = TranslationLangsOptionHelper(dpmSectionOptions)

        return SectionPlanSql.withSingleQuery(
            sectionOutline = planComposer.sectionOutline(
                sectionShortTitle = "DictTranslation",
                sectionTitle = "Dictionary translations",
                sectionDescription = "Label and description changes in Domains, Members, Metrics, Dimensions and Hierarchies"
            ),

            queryColumnMapping = planComposer.queryColumnMapping(),

            query = planComposer.query(),

            sourceTableDescriptors = listOf(
                translationLangsOptionHelper.sourceTableDescriptor(
                    elementTable = "mDomain",
                    conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mDomain.ConceptID"
                ),

                translationLangsOptionHelper.sourceTableDescriptor(
                    elementTable = "mMember",
                    conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mMember.ConceptID"
                ),

                translationLangsOptionHelper.sourceTableDescriptor(
                    elementTable = "mDimension",
                    conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mDimension.ConceptID"
                ),

                translationLangsOptionHelper.sourceTableDescriptor(
                    elementTable = "mHierarchy",
                    conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mHierarchy.ConceptID"
                )
            )
        )
    }
}
