package fi.vm.dpm.diff.repgen.vkdata.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
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
import fi.vm.dpm.diff.repgen.PartitionHelper
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.SourceDbs

object RequiredSection {

    fun sectionPlan(sourceDbs: SourceDbs): SectionPlanSql {

        val rowid = FallbackField(
            fieldName = "Rowid"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(rowid)
        )

        val frameworkCode = KeyField(
            fieldName = "FrameworkCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = null
        )

        val taxonomyCode = KeyField(
            fieldName = "TaxonomyCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = null
        )

        val datapointID = KeyField(
            fieldName = "DatapointID",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = null
        )

        val changeKind = ChangeKindField()

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "Required",
            sectionTitle = "Required",
            sectionDescription = "Added and deleted reporting requirements",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                rowid,
                recordIdentityFallback,
                frameworkCode,
                taxonomyCode,
                datapointID,
                changeKind,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSortBy(frameworkCode),
                NumberAwareSortBy(taxonomyCode),
                NumberAwareSortBy(datapointID),
                FixedChangeKindSortBy(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "Rowid" to rowid,
            "FrameworkCode" to frameworkCode,
            "TaxonomyCode" to taxonomyCode,
            "DatapointID" to datapointID
        )

        val partitionCount = PartitionHelper.getPartitionCount(
            sourceDbs,
            "Required"
        )

        val partitionCriteriaRange = 0.until(partitionCount)

        val queries = partitionCriteriaRange.map { partitionCriteria ->
            """
            SELECT
            rowid AS 'Rowid'
            ,Required.FrameworkCode AS 'FrameworkCode'
            ,Required.TaxonomyCode AS 'TaxonomyCode'
            ,Required.DatapointID AS 'DatapointID'

            FROM Required

            ${if (partitionCount > 1) "WHERE Required.DatapointID % $partitionCount = $partitionCriteria" else ""}

            """.trimLineStartsAndConsequentBlankLines()
        }

        val sourceTableDescriptors = listOf(
            "Required"
        )

        return SectionPlanSql.withPartitionedQueries(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            partitionedQueries = queries,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
