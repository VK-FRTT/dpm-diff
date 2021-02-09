package fi.vm.dpm.diff.repgen.dpm.dictionary

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeDetectionMode
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSortBy
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.NumberAwareSortBy
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.dpm.utils.DpmSectionIdentificationLabels

object DimensionSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val dimensionId = FallbackField(
            fieldName = "DimensionId"
        )

        val dimensionInherentLabel = FallbackField(
            fieldName = "DimensionLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(dimensionId, dimensionInherentLabel)
        )

        val dimensionCode = KeyField(
            fieldName = "DimensionCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = dimensionInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "DimensionLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        val referencedDomainCode = AtomField(
            fieldName = "ReferencedDomainCode"
        )

        val isTypedDimension = AtomField(
            fieldName = "IsTypedDimension"
        )

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "Dimension",
            sectionTitle = "Dimensions",
            sectionDescription = "Added and deleted Dimensions, changes in Domain reference and IsTypedDimension",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                dimensionId,
                dimensionInherentLabel,
                recordIdentityFallback,
                dimensionCode,
                *identificationLabels.labelFields(),
                changeKind,
                referencedDomainCode,
                isTypedDimension,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSortBy(dimensionCode),
                FixedChangeKindSortBy(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "DimensionId" to dimensionId,
            "DimensionInherentLabel" to dimensionInherentLabel,
            "DimensionCode" to dimensionCode,
            *identificationLabels.labelColumnMapping(),
            "DomainCode" to referencedDomainCode,
            "IsTypedDimension" to isTypedDimension
        )

        val query = """
            SELECT
            mDimension.DimensionID AS 'DimensionId'
            ,mDimension.DimensionLabel AS 'DimensionInherentLabel'
            ,mDimension.DimensionCode AS 'DimensionCode'
            ${identificationLabels.labelAggregateFragment()}
            ,mDomain.DomainCode AS 'DomainCode'
            ,mDimension.IsTypedDimension AS 'IsTypedDimension'

            FROM mDimension
            LEFT JOIN mDomain ON mDomain.DomainID = mDimension.DomainID
            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mDimension.ConceptID
            LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID

            WHERE
            mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

            GROUP BY mDimension.DimensionID

            ORDER BY mDomain.DomainCode ASC, mDimension.DimensionCode ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mDimension"
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
