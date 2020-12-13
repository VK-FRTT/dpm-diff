package fi.vm.dpm.diff.repgen.dpm.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.AtomOption
import fi.vm.dpm.diff.model.ChangeDetectionMode
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.DisplayHint
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.FixedTranslationRoleSort
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.dpm.utils.DpmSectionIdentificationLabels
import fi.vm.dpm.diff.repgen.dpm.utils.TranslationLangsOptionHelper

object AxisOrdinateTranslationSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val translationLangsOptionHelper = TranslationLangsOptionHelper(dpmSectionOptions)

        // Context parents
        val taxonomyInherentLabel = FallbackField(
            fieldName = "TaxonomyLabel"
        )

        val taxonomyCode = KeyField(
            fieldName = "TaxonomyCode",
            keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
            keyFieldFallback = taxonomyInherentLabel
        )

        val tableInherentLabel = FallbackField(
            fieldName = "TableLabel"
        )

        val tableCode = KeyField(
            fieldName = "TableCode",
            keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
            keyFieldFallback = tableInherentLabel
        )

        val axisInherentLabel = FallbackField(
            fieldName = "AxisLabel"
        )

        val axisOrientation = KeyField(
            fieldName = "AxisOrientation",
            keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
            keyFieldFallback = axisInherentLabel
        )

        // AxisOrdinate (parent)
        val ordinateId = FallbackField(
            fieldName = "OrdinateId"
        )

        val ordinateInherentLabel = FallbackField(
            fieldName = "OrdinateLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(ordinateId, ordinateInherentLabel)
        )

        val ordinateCode = KeyField(
            fieldName = "OrdinateCode",
            keyFieldKind = KeyFieldKind.PARENT_KEY,
            keyFieldFallback = ordinateInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "OrdinateLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        // Translations
        val translationRole = KeyField(
            fieldName = "TranslationRole",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = null
        )

        val translationLanguage = KeyField(
            fieldName = "Language",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = null
        )

        val changeKind = ChangeKindField()

        val translation = AtomField(
            fieldName = "Translation",
            displayHint = DisplayHint.FIXED_EXTRA_WIDE,
            atomOptions = listOf(AtomOption.OUTPUT_TO_ADDED_CHANGE)
        )
        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "AxisOrdTranslation",
            sectionTitle = "AxisOrdinate translations",
            sectionDescription = "Label and description changes in Axis Ordinates",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS_AND_REQUIRE_PARENT_EXISTENCE,
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
                *identificationLabels.labelFields(),
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
            includedChanges = ChangeKind.allExceptDuplicateKeyAlerts()
        )

        val queryColumnMapping = mapOf(
            "TaxonomyInherentLabel" to taxonomyInherentLabel,
            "TaxonomyCode" to taxonomyCode,
            "TableInherentLabel" to tableInherentLabel,
            "TableCode" to tableCode,
            "AxisInherentLabel" to axisInherentLabel,
            "AxisOrientation" to axisOrientation,
            "OrdinateId" to ordinateId,
            "OrdinateInherentLabel" to ordinateInherentLabel,
            "OrdinateCode" to ordinateCode,
            *identificationLabels.labelColumnMapping(),
            "TranslationRole" to translationRole,
            "TranslationLanguage" to translationLanguage,
            "Translation" to translation
        )

        val query = """
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
             ${identificationLabels.labelAggregateFragment()}

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
            ${identificationLabels.labelColumnNamesFragment()}
            ,mConceptTranslation.Role AS TranslationRole
            ,mLanguage.IsoCode AS TranslationLanguage
            ,mConceptTranslation.Text AS Translation

            FROM
            AxisOrdinateOverview

            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = OrdinateConceptId
            LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID

            ${translationLangsOptionHelper.translationLanguageWhereStatement()}

            ORDER BY OrdinateCode

            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            translationLangsOptionHelper.sourceTableDescriptor(
                elementTable = "mAxisOrdinate",
                conceptTranslationJoin = "mConceptTranslation on mConceptTranslation.ConceptID = mAxisOrdinate.ConceptID"
            )
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
