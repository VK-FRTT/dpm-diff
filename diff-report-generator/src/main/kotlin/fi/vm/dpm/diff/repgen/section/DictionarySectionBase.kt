package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.CorrelationKeyKind
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

open class DictionarySectionBase(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    protected val elementId = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "ElementId"
    )

    protected val elementInherentLabel = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "ElementLabel"
    )

    protected val elementType = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "ElementType",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY
    )

    protected val elementCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "ElementCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY,
        correlationFallback = elementInherentLabel,
        noteFields = listOf(elementId, elementInherentLabel)
    )

    protected val parentElementCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "ParentElementCode",
        correlationKeyKind = CorrelationKeyKind.PRIMARY_KEY
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "ElementLabel",
        noteField = elementInherentLabel
    )

    protected val elementQueryDescriptors: List<ElementQueryDescriptor> =
        listOf(
            ElementQueryDescriptor(
                elementType = "Domain",
                elementTableName = "mDomain",
                elementIdColumn = "DomainID",
                elementCodeColumn = "DomainCode",
                elementInherentLabelColumn = "DomainLabel",
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
                parentCodeStatement = "mDomain.DomainCode",
                parentTableJoin = "JOIN mDomain on mDomain.DomainID = mHierarchy.DomainID",
                elementTableSliceCriteria = ""
            )
        )

    protected fun elementEssentialsQueryExpression(
        elementQueryDescription: ElementQueryDescriptor
    ): String {

        val queryName = elementEssentialsQueryName(elementQueryDescription)

        // TODO provide parent type as separate column + combine on Excel output level

        return with(elementQueryDescription) {
            """
            $queryName AS (

            SELECT
            '$elementType' AS ElementType
            ,$elementTableName.$elementIdColumn AS ElementId
            ,$elementTableName.ConceptID AS ElementConceptId
            ,$elementTableName.$elementInherentLabelColumn AS ElementInherentLabel
            ,$elementTableName.$elementCodeColumn AS ElementCode
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
