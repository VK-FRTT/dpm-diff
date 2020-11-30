package fi.vm.dpm.diff.repgen.dpm.section.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.AtomOption
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.DisplayHint
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.FixedTranslationRoleSort
import fi.vm.dpm.diff.model.KeySegmentField
import fi.vm.dpm.diff.model.KeySegmentKind
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.dpm.DpmGenerationContext
import fi.vm.dpm.diff.repgen.dpm.section.ElementTranslationHelpers.elementTranslationSourceTableDescriptor
import fi.vm.dpm.diff.repgen.dpm.section.ElementTranslationHelpers.translationLanguageWhereStatement
import fi.vm.dpm.diff.repgen.dpm.section.SectionBase

class AxisOrdinateTranslationSection(
    generationContext: DpmGenerationContext,
    translationLangCodes: List<String>?
) : SectionBase(
    generationContext
) {

    // Scope
    private val taxonomyInherentLabel = FallbackField(
        fieldName = "TaxonomyLabel"
    )

    private val taxonomyCode = KeySegmentField(
        fieldName = "TaxonomyCode",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = taxonomyInherentLabel
    )

    private val tableInherentLabel = FallbackField(
        fieldName = "TableLabel"
    )

    private val tableCode = KeySegmentField(
        fieldName = "TableCode",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = tableInherentLabel
    )

    private val axisInherentLabel = FallbackField(
        fieldName = "AxisLabel"
    )

    private val axisOrientation = KeySegmentField(
        fieldName = "AxisOrientation",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = axisInherentLabel
    )

    // AxisOrdinate
    private val ordinateId = FallbackField(
        fieldName = "OrdinateId"
    )

    private val ordinateInherentLabel = FallbackField(
        fieldName = "OrdinateLabel"
    )

    private val recordIdentityFallback = RecordIdentityFallbackField(
        identityFallbacks = listOf(ordinateId, ordinateInherentLabel)
    )

    private val ordinateCode = KeySegmentField(
        fieldName = "OrdinateCode",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = ordinateInherentLabel
    )

    override val identificationLabels = idLabelFields(
        fieldNameBase = "OrdinateLabel"
    )

    // Translations
    private val translationRole = KeySegmentField(
        fieldName = "TranslationRole",
        segmentKind = KeySegmentKind.SUB_SEGMENT,
        segmentFallback = null
    )

    private val translationLanguage = KeySegmentField(
        fieldName = "Language",
        segmentKind = KeySegmentKind.SUB_SEGMENT,
        segmentFallback = null
    )

    private val translation = AtomField(
        fieldName = "Translation",
        displayHint = DisplayHint.FIXED_EXTRA_WIDE,
        atomOptions = AtomOption.OUTPUT_TO_ADDED_CHANGE
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "AxisOrdTranslation",
        sectionTitle = "AxisOrdinate translations",
        sectionDescription = "Label and description changes in Axis Ordinates",
        sectionFields = listOf(
            taxonomyInherentLabel,
            taxonomyCode,
            tableInherentLabel,
            tableCode,
            axisInherentLabel,
            axisOrientation,
            ordinateId,
            ordinateInherentLabel,
            recordIdentityFallback,
            ordinateCode,
            *identificationLabels,
            translationRole,
            translationLanguage,
            changeKind,
            translation,
            note
        ),
        sectionSortOrder = listOf(
            NumberAwareSort(taxonomyCode),
            NumberAwareSort(tableCode),
            NumberAwareSort(axisOrientation),
            NumberAwareSort(ordinateCode),
            FixedTranslationRoleSort(translationRole),
            NumberAwareSort(translationLanguage),
            FixedChangeKindSort(changeKind)
        ),
        includedChanges = ChangeKind.allWithoutDuplicateKeyChanges()
    )

    override val queryColumnMapping = mapOf(
        "TaxonomyInherentLabel" to taxonomyInherentLabel,
        "TaxonomyCode" to taxonomyCode,
        "TableInherentLabel" to tableInherentLabel,
        "TableCode" to tableCode,
        "AxisInherentLabel" to axisInherentLabel,
        "AxisOrientation" to axisOrientation,
        "OrdinateId" to ordinateId,
        "OrdinateInherentLabel" to ordinateInherentLabel,
        "OrdinateCode" to ordinateCode,
        *idLabelColumnMapping(),
        "TranslationRole" to translationRole,
        "TranslationLanguage" to translationLanguage,
        "Translation" to translation
    )

    override val query = """
        WITH AxisOrdinateOverview AS (
        SELECT
        mTaxonomy.TaxonomyLabel AS 'TaxonomyInherentLabel'
        ,mTaxonomy.TaxonomyCode AS 'TaxonomyCode'
        ,mTable.TableLabel AS 'TableInherentLabel'
        ,mTable.TableCode AS 'TableCode'
        ,mAxis.AxisLabel AS 'AxisInherentLabel'
        ,mAxis.AxisOrientation AS 'AxisOrientation'
        ,mAxisOrdinate.OrdinateID AS 'OrdinateId'
        ,mAxisOrdinate.OrdinateLabel AS 'OrdinateInherentLabel'
        ,mAxisOrdinate.OrdinateCode AS 'OrdinateCode'
        ,mAxisOrdinate.ConceptID AS 'OrdinateConceptId'
        ${idLabelAggregateFragment()}

		FROM mAxisOrdinate
        LEFT JOIN mAxis ON mAxis.AxisID = mAxisOrdinate.AxisID
        LEFT JOIN mTableAxis ON mTableAxis.AxisID = mAxisOrdinate.AxisID
        LEFT JOIN mTable ON mTable.TableID = mTableAxis.TableID
        LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
        LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mAxisOrdinate.ConceptID
        LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID


        WHERE
        (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

        GROUP BY mAxisOrdinate.OrdinateID
        )

        SELECT
        TaxonomyInherentLabel AS 'TaxonomyInherentLabel'
        ,TaxonomyCode AS 'TaxonomyCode'
        ,TableInherentLabel AS 'TableInherentLabel'
        ,TableCode AS 'TableCode'
        ,AxisInherentLabel AS 'AxisInherentLabel'
        ,AxisOrientation AS 'AxisOrientation'
        ,OrdinateId AS 'OrdinateId'
        ,OrdinateInherentLabel AS 'OrdinateInherentLabel'
        ,OrdinateCode AS 'OrdinateCode'
        ${idLabelColumnNamesFragment()}
        ,mConceptTranslation.Role AS TranslationRole
        ,mLanguage.IsoCode AS TranslationLanguage
        ,mConceptTranslation.Text AS Translation

        FROM
        AxisOrdinateOverview

        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = OrdinateConceptId
        LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID

        ${translationLanguageWhereStatement(translationLangCodes)}

        ORDER BY OrdinateCode

    """.trimLineStartsAndConsequentBlankLines()

    override val sourceTableDescriptors = listOf(
        elementTranslationSourceTableDescriptor(
            elementTable = "mAxisOrdinate",
            conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mAxisOrdinate.ConceptID",
            translationLangCodes = translationLangCodes
        )
    )

    init {
        sanityCheckSectionConfig()
    }
}
