package fi.vm.dpm.diff.repgen.dpm.dictionary

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.KeySegmentField
import fi.vm.dpm.diff.model.KeySegmentKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.dpm.utils.DpmSectionIdentificationLabels

object DomainSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val domainId = FallbackField(
            fieldName = "DomainId"
        )

        val domainInherentLabel = FallbackField(
            fieldName = "DomainLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(domainId, domainInherentLabel)
        )

        val domainCode = KeySegmentField(
            fieldName = "DomainCode",
            segmentKind = KeySegmentKind.PRIME_SEGMENT,
            segmentFallback = domainInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "DomainLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        val isTypedDomain = AtomField(
            fieldName = "IsTypedDomain"
        )

        val dataType = AtomField(
            fieldName = "TypedDomainDataType"
        )

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "Domain",
            sectionTitle = "Domains",
            sectionDescription = "Added and deleted Domains, changes in IsTypedDomain and DataType",
            sectionFields = listOf(
                domainId,
                domainInherentLabel,
                recordIdentityFallback,
                domainCode,
                *identificationLabels.labelFields(),
                changeKind,
                isTypedDomain,
                dataType,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSort(domainCode),
                FixedChangeKindSort(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "DomainId" to domainId,
            "DomainInherentLabel" to domainInherentLabel,
            "DomainCode" to domainCode,
            *identificationLabels.labelColumnMapping(),
            "DataType" to dataType,
            "IsTypedDomain" to isTypedDomain
        )

        val query = """
            SELECT
            mDomain.DomainID AS 'DomainId'
            ,mDomain.DomainLabel AS 'DomainInherentLabel'
            ,mDomain.DomainCode AS 'DomainCode'
             ${identificationLabels.labelAggregateFragment()}
            ,mDomain.DataType AS 'DataType'
            ,mDomain.IsTypedDomain AS 'IsTypedDomain'

            FROM mDomain
            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mDomain.ConceptID
            LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID

            WHERE
            mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

            GROUP BY mDomain.DomainID

            ORDER BY mDomain.DomainCode ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mDomain"
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
