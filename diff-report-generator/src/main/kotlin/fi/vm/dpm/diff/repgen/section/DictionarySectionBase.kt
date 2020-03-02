package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.CorrelationKeyField
import fi.vm.dpm.diff.model.CorrelationKeyKind
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

open class DictionarySectionBase(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    protected val elementId = FallbackField(
        fieldName = "ElementId"
    )

    protected val elementInherentLabel = FallbackField(
        fieldName = "ElementLabel"
    )

    protected val parentElementType = CorrelationKeyField(
        fieldName = "ParentElementType",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = null,
        noteFallbacks = emptyList()
    )

    protected val parentElementCode = CorrelationKeyField(
        fieldName = "ParentElementCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = null,
        noteFallbacks = emptyList()
    )

    protected val elementType = CorrelationKeyField(
        fieldName = "ElementType",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = null,
        noteFallbacks = emptyList()
    )

    protected val elementCode = CorrelationKeyField(
        fieldName = "ElementCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = elementInherentLabel,
        noteFallbacks = listOf(elementId, elementInherentLabel)
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "ElementLabel",
        fallbackField = elementInherentLabel
    )

    protected val elementQueryDescriptors: List<ElementQueryDescriptor> =
        listOf(
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

    protected fun elementEssentialsQueryExpression(
        elementQueryDescription: ElementQueryDescriptor
    ): String {

        val queryName = elementEssentialsQueryName(elementQueryDescription)

        return with(elementQueryDescription) {
            """
            $queryName AS (

            SELECT
            '$elementType' AS ElementType
            ,$elementTableName.$elementIdColumn AS ElementId
            ,$elementTableName.ConceptID AS ElementConceptId
            ,$elementTableName.$elementInherentLabelColumn AS ElementInherentLabel
            ,$elementTableName.$elementCodeColumn AS ElementCode
            ,'$parentType' AS ParentElementType
            ,$parentCodeStatement AS ParentElementCode
            ${idLabelAggregateFragment()}

            FROM
            $elementTableName

            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = $elementTableName.ConceptID
            LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID
            $parentTableJoin

            WHERE
            (mConceptTranslation.Role = 'label' OR mConceptTranslation.Role IS NULL)
            $elementTableSliceCriteria

            GROUP BY $elementIdColumn
            )
            """.trimLineStartsAndConsequentBlankLines()
        }
    }

    protected fun elementEssentialsQueryName(
        elementQueryDescription: ElementQueryDescriptor
    ): String {
        return "${elementQueryDescription.elementType}Essentials"
    }
}
