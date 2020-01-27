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
    private val dictionaryElementCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_ID,
        fieldName = "dictionary element code"
    )

    private val dictionaryElementType = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_ID,
        fieldName = "dictionary element type"
    )

    override val identificationLabels = composeIdentificationLabels {
        "dictionary element label $it"
    }

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Dict Elem Overview",
        sectionTitle = "Dictionary elements overview",
        sectionDescription = "Dictionary elements: added and removed Domains, Members, Metrics, Dimensions and Hierarchies",
        sectionFields = listOf(
            dictionaryElementCode,
            dictionaryElementType,
            *identificationLabels,
            differenceKind
        )
    )

    override val query = """
        SELECT
        ElementCode AS 'ElementCode'
        ,ElementType AS 'ElementType'
        ${composeIdentificationLabelQueryFragment(
        "IdLabelIsoCode",
        "IdLabelText"
    )}

        FROM (

        ${composeDictionaryElementQueryFragment(
        "Domain",
        "mDomain",
        "DomainID",
        "mDomain.DomainCode"
    )}

        UNION ALL

        ${composeDictionaryElementQueryFragment(
        "Member",
        "mMember",
        "MemberID",
        "mMember.MemberCode",
        "",
        "AND mMember.MemberID NOT IN (SELECT CorrespondingMemberID FROM mMetric)"
    )}

        UNION ALL

        ${composeDictionaryElementQueryFragment(
        "Metric",
        "mMember",
        "MemberID",
        "mMember.MemberCode",
        "",
        "AND mMember.MemberID IN (SELECT CorrespondingMemberID FROM mMetric)"
    )}

        UNION ALL

        ${composeDictionaryElementQueryFragment(
        "Dimension",
        "mDimension",
        "DimensionID",
        "mDimension.DimensionCode"
    )}

        UNION ALL

        ${composeDictionaryElementQueryFragment(
        "Hierarchy",
        "mHierarchy",
        "HierarchyID",
        "mDomain.DomainCode || ':' || mHierarchy.HierarchyCode",
        "JOIN mDomain on mDomain.DomainID = mHierarchy.DomainID"
    )}
        )

        GROUP BY ElementType, ElementID

    """.trimLineStartsAndConsequentBlankLines()

    override val queryPrimaryTables = listOf("mDomain", "mMember", "mDimension", "mHierarchy")

    override val columnNames = mapOf(
        "ElementCode" to dictionaryElementCode,
        "ElementType" to dictionaryElementType,
        *composeIdentificationLabelColumnNames()
    )

    private fun composeDictionaryElementQueryFragment(
        elementType: String,
        elementTableName: String,
        elementIdColumnName: String,
        elementCodeStatement: String,
        additionalJoin: String = "",
        additionalWhereCriteria: String = ""
    ): String {
        return """
        SELECT
        '$elementType' AS 'ElementType'
        ,$elementTableName.$elementIdColumnName AS 'ElementID'
        ,$elementCodeStatement AS 'ElementCode'
        ,mLanguage.IsoCode AS 'IdLabelIsoCode'
        ,mConceptTranslation.Text AS 'IdLabelText'

        FROM
        $elementTableName

        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = $elementTableName.ConceptID
        LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID
        $additionalJoin

        WHERE
        (mConceptTranslation.Role = 'label' OR mConceptTranslation.Role IS NULL)
        $additionalWhereCriteria
    """
    }
}
