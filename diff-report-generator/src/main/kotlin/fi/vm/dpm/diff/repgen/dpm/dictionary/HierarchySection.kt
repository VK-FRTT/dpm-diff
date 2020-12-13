package fi.vm.dpm.diff.repgen.dpm.dictionary

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.ChangeDetectionMode
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.dpm.utils.DpmSectionIdentificationLabels

object HierarchySection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val domainInherentLabel = FallbackField(
            fieldName = "DomainLabel"
        )

        val domainCode = KeyField(
            fieldName = "DomainCode",
            keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
            keyFieldFallback = domainInherentLabel
        )

        val hierarchyId = FallbackField(
            fieldName = "HierarchyId"
        )

        val hierarchyInherentLabel = FallbackField(
            fieldName = "HierarchyLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(hierarchyId, hierarchyInherentLabel)
        )

        val hierarchyCode = KeyField(
            fieldName = "HierarchyCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = hierarchyInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "HierarchyLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "Hierarchy",
            sectionTitle = "Hierarchies",
            sectionDescription = "Added and deleted Hierarchies",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                domainInherentLabel,
                domainCode,
                hierarchyId,
                hierarchyInherentLabel,
                recordIdentityFallback,
                hierarchyCode,
                *identificationLabels.labelFields(),
                changeKind,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSort(domainCode),
                NumberAwareSort(hierarchyCode),
                FixedChangeKindSort(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "DomainInherentLabel" to domainInherentLabel,
            "DomainCode" to domainCode,
            "HierarchyId" to hierarchyId,
            "HierarchyInherentLabel" to hierarchyInherentLabel,
            "HierarchyCode" to hierarchyCode,
            *identificationLabels.labelColumnMapping()
        )

        val query = """
            SELECT
            mDomain.DomainLabel AS 'DomainInherentLabel'
            ,mDomain.DomainCode AS 'DomainCode'
            ,mHierarchy.HierarchyID AS 'HierarchyId'
            ,mHierarchy.HierarchyLabel AS 'HierarchyInherentLabel'
            ,mHierarchy.HierarchyCode AS 'HierarchyCode'
             ${identificationLabels.labelAggregateFragment()}

            FROM mHierarchy
            LEFT JOIN mDomain on mDomain.DomainID = mHierarchy.DomainID
            LEFT JOIN mConceptTranslation on mConceptTranslation.ConceptID = mHierarchy.ConceptID
            LEFT JOIN mLanguage on mLanguage.LanguageID = mConceptTranslation.LanguageID

            WHERE
            mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

            GROUP BY mHierarchy.HierarchyID

            ORDER BY mDomain.DomainCode ASC, mHierarchy.HierarchyCode ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mHierarchy"
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
