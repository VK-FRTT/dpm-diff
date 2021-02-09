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

object MetricSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val domainInherentLabel = FallbackField(
            fieldName = "DomainLabel"
        )

        val domainCode = KeyField(
            fieldName = "DomainCode",
            keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
            keyFieldFallback = domainInherentLabel
        )

        val metricId = FallbackField(
            fieldName = "MetricId"
        )

        val metricInherentLabel = FallbackField(
            fieldName = "MetricLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(metricId, metricInherentLabel)
        )

        val metricCode = KeyField(
            fieldName = "MetricCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = metricInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "MetricLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        val dataType = AtomField(
            fieldName = "DataType"
        )

        val flowType = AtomField(
            fieldName = "FlowType"
        )

        val balanceType = AtomField(
            fieldName = "BalanceType"
        )

        val referencedDomainCode = AtomField(
            fieldName = "ReferencedDomain"
        )

        val referencedHierarchyCode = AtomField(
            fieldName = "ReferencedHierarchy"
        )

        val hierarchyStartingMemberCode = AtomField(
            fieldName = "HierarchyStartingMember"
        )

        val isStartingMemberIncluded = AtomField(
            fieldName = "IsStartingMemberIncluded"
        )
        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "Metric",
            sectionTitle = "Metrics",
            sectionDescription = "Added and deleted Metrics, changes in DataType, FlowType, BalanceType, Domain reference and Hierarchy reference",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                domainInherentLabel,
                domainCode,
                metricId,
                metricInherentLabel,
                recordIdentityFallback,
                metricCode,
                *identificationLabels.labelFields(),
                changeKind,
                dataType,
                flowType,
                balanceType,
                referencedDomainCode,
                referencedHierarchyCode,
                hierarchyStartingMemberCode,
                isStartingMemberIncluded,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSortBy(domainCode),
                NumberAwareSortBy(metricCode),
                FixedChangeKindSortBy(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "DomainInherentLabel" to domainInherentLabel,
            "DomainCode" to domainCode,
            "MetricId" to metricId,
            "MetricInherentLabel" to metricInherentLabel,
            "MetricCode" to metricCode,
            *identificationLabels.labelColumnMapping(),
            "DataType" to dataType,
            "FlowType" to flowType,
            "BalanceType" to balanceType,
            "ReferencedDomainCode" to referencedDomainCode,
            "ReferencedHierarchyCode" to referencedHierarchyCode,
            "HierarchyStartingMemberCode" to hierarchyStartingMemberCode,
            "IsStartingMemberIncluded" to isStartingMemberIncluded
        )

        val query = """
            SELECT
            mDomain.DomainLabel AS 'DomainInherentLabel'
            ,mDomain.DomainCode AS 'DomainCode'
            ,mMetric.MetricID AS 'MetricId'
            ,mMember.MemberLabel AS 'MetricInherentLabel'
            ,mMember.MemberCode AS 'MetricCode'
             ${identificationLabels.labelAggregateFragment()}
            ,mMetric.DataType AS 'DataType'
            ,mMetric.FlowType AS 'FlowType'
            ,mMetric.BalanceType AS 'BalanceType'
            ,ReferencedDomain.DomainCode AS 'ReferencedDomainCode'
            ,ReferencedHierarchy.HierarchyCode AS 'ReferencedHierarchyCode'
            ,HierarchyStartingMember.MemberCode AS 'HierarchyStartingMemberCode'
            ,mMetric.IsStartingMemberIncluded AS 'IsStartingMemberIncluded'

            FROM mMetric
            LEFT JOIN mMember ON mMember.MemberID = mMetric.CorrespondingMemberID
            LEFT JOIN mDomain ON mDomain.DomainID = mMember.DomainID
            LEFT JOIN mDomain AS ReferencedDomain ON ReferencedDomain.DomainID = mMetric.ReferencedDomainID
            LEFT JOIN mHierarchy AS ReferencedHierarchy ON ReferencedHierarchy.HierarchyID = mMetric.ReferencedHierarchyID
            LEFT JOIN mMember AS HierarchyStartingMember ON HierarchyStartingMember.MemberID = mMetric.HierarchyStartingMemberID
            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mMember.ConceptID
            LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID

            WHERE
            mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

            GROUP BY mMetric.MetricID

            ORDER BY mDomain.DomainCode ASC, mMember.MemberCode ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mMetric"
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
