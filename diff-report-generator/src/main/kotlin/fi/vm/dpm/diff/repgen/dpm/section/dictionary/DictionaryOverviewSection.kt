package fi.vm.dpm.diff.repgen.dpm.section.dictionary

import fi.vm.dpm.diff.repgen.dpm.DpmGenerationContext
import fi.vm.dpm.diff.repgen.dpm.section.ElementOverviewSectionBase
import fi.vm.dpm.diff.repgen.dpm.section.ElementQueryDescriptor

class DictionaryOverviewSection(
    generationContext: DpmGenerationContext
) : ElementOverviewSectionBase(
    generationContext
) {
    companion object {
        val elementQueryDescriptors = listOf(
            ElementQueryDescriptor(
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

            ElementQueryDescriptor(
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

            ElementQueryDescriptor(
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

            ElementQueryDescriptor(
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

            ElementQueryDescriptor(
                elementType = "Hierarchy",
                elementTableName = "mHierarchy",
                elementIdColumn = "HierarchyID",
                elementCodeColumn = "HierarchyCode",
                elementInherentLabelColumn = "HierarchyLabel",
                parentType = "Domain",
                parentCodeStatement = "mDomain.DomainCode",
                parentTableJoin = "JOIN mDomain on mDomain.DomainID = mHierarchy.DomainID",
                elementTableSliceCriteria = ""
            )
        )
    }

    override val sectionDescriptor = elementOverviewSectionDescriptor(
        sectionShortTitle = "DictOverview",
        sectionTitle = "Dictionary overview",
        sectionDescription = "Added and deleted Domains, Members, Metrics, Dimensions and Hierarchies"
    )

    override val queryColumnMapping = elementOverviewQueryColumnMappings()

    override val query = elementOverviewQuery(DictionaryOverviewSection.elementQueryDescriptors)

    override val sourceTableDescriptors = listOf(
        "mDomain",
        "mMember",
        "mDimension",
        "mHierarchy"
    )

    init {
        sanityCheckSectionConfig()
    }
}
