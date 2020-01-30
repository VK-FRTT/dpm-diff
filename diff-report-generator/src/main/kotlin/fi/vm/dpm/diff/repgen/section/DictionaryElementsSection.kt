package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class DictionaryElementsSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    private val elementId = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "ElementId"
    )

    private val elementInherentLabel = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "ElementLabel"
    )

    private val elementType = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "Element type"
    )

    private val elementCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "Element code",
        correlationKeyFallback = elementInherentLabel,
        noteFallback = listOf(elementId, elementInherentLabel)
    )

    private val parentElementCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "Parent element code"
    )

    override val identificationLabels = composeIdentificationLabelFields(
        noteFallback = elementInherentLabel
    ) {
        "Element label $it"
    }

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Dict Elem Overview",
        sectionTitle = "Dictionary elements overview",
        sectionDescription = "Dictionary elements: added and removed Domains, Members, Metrics, Dimensions and Hierarchies",
        sectionFields = listOf(
            elementId,
            elementInherentLabel,
            elementType,
            elementCode,
            parentElementCode,
            *identificationLabels,
            differenceKind,
            note
        )
    )

    private data class ElementTypeParams(
        val elementType: String,
        val elementTableName: String,
        val elementIdColumn: String,
        val elementCodeColumn: String,
        val elementInherentLabelColumn: String,
        val parentCodeStatement: String,
        val parentTableJoin: String,
        val elementTableSliceCriteria: String
    )

    override val query = run {

        val elementTypes = listOf(
            ElementTypeParams(
                elementType = "Domain",
                elementTableName = "mDomain",
                elementIdColumn = "DomainID",
                elementCodeColumn = "DomainCode",
                elementInherentLabelColumn = "DomainLabel",
                parentCodeStatement = "NULL",
                parentTableJoin = "",
                elementTableSliceCriteria = ""
            ),

            ElementTypeParams(
                elementType = "Member",
                elementTableName = "mMember",
                elementIdColumn = "MemberID",
                elementCodeColumn = "MemberCode",
                elementInherentLabelColumn = "MemberLabel",
                parentCodeStatement = "mDomain.DomainCode",
                parentTableJoin = "LEFT JOIN mDomain ON mDomain.DomainID = mMember.DomainID",
                elementTableSliceCriteria = "AND mMember.MemberID NOT IN (SELECT CorrespondingMemberID FROM mMetric)"
            ),

            ElementTypeParams(
                elementType = "Metric",
                elementTableName = "mMember",
                elementIdColumn = "MemberID",
                elementCodeColumn = "MemberCode",
                elementInherentLabelColumn = "MemberLabel",
                parentCodeStatement = "mDomain.DomainCode",
                parentTableJoin = "LEFT JOIN mDomain ON mDomain.DomainID = mMember.DomainID",
                elementTableSliceCriteria = "AND mMember.MemberID IN (SELECT CorrespondingMemberID FROM mMetric)"
            ),

            ElementTypeParams(
                elementType = "Dimension",
                elementTableName = "mDimension",
                elementIdColumn = "DimensionID",
                elementCodeColumn = "DimensionCode",
                elementInherentLabelColumn = "DimensionLabel",
                parentCodeStatement = "mDomain.DomainCode",
                parentTableJoin = "LEFT JOIN mDomain ON mDomain.DomainID = mDimension.DomainID",
                elementTableSliceCriteria = ""
            ),

            ElementTypeParams(
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

        val query =
            """
            SELECT
            ElementId AS 'ElementId'
            ,ElementInherentLabel AS 'ElementInherentLabel'
            ,ElementType AS 'ElementType'
            ,ElementCode AS 'ElementCode'
            ,ParentElementCode AS 'ParentElementCode'
            ${composeIdentificationLabelQueryFragment(
                criteriaLangColumn = "IdLabelIsoCode",
                sourceTextColumn = "IdLabelText"
            )}

            FROM (
                ${elementTypes.map(::elementQueryFragment).joinToString("\nUNION ALL\n")}
            )

            GROUP BY ElementTable, ElementId

            ORDER BY ElementType, ParentElementCode, ElementCode
            """

        query.trimLineStartsAndConsequentBlankLines()
    }

    override val primaryTables = listOf("mDomain", "mMember", "mDimension", "mHierarchy")

    override val queryColumnMapping = mapOf(
        "ElementId" to elementId,
        "ElementInherentLabel" to elementInherentLabel,
        "ElementType" to elementType,
        "ElementCode" to elementCode,
        "ParentElementCode" to parentElementCode,
        *composeIdentificationLabelColumnNames()
    )

    private fun elementQueryFragment(
        params: ElementTypeParams
    ): String {

        return with(params) {
            """
            SELECT
            '$elementTableName' AS 'ElementTable'
            ,'$elementType' AS 'ElementType'
            ,$elementTableName.$elementIdColumn AS 'ElementId'
            ,$elementTableName.$elementInherentLabelColumn AS 'ElementInherentLabel'
            ,$elementTableName.$elementCodeColumn AS 'ElementCode'
            ,$parentCodeStatement AS 'ParentElementCode'
            ,mLanguage.IsoCode AS 'IdLabelIsoCode'
            ,mConceptTranslation.Text AS 'IdLabelText'

            FROM
            $elementTableName

            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = $elementTableName.ConceptID
            LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID
            $parentTableJoin

            WHERE
            (mConceptTranslation.Role = 'label' OR mConceptTranslation.Role IS NULL)
            $elementTableSliceCriteria
            """
        }
    }
}
