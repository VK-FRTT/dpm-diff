package fi.vm.dpm.diff.repgen.vkdata.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.AtomOption
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

object DataPointIdSection {

    fun sectionPlan(sourceDbs: SourceDbs): SectionPlanSql {

        val rowid = FallbackField(
            fieldName = "Rowid"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(rowid)
        )

        val datapointID = KeyField(
            fieldName = "DatapointID",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = null
        )

        val changeKind = ChangeKindField()

        val dpsFull = AtomField(
            fieldName = "DpsFull",
            atomOptions = listOf(
                AtomOption.OUTPUT_TO_ADDED_CHANGE,
                AtomOption.OUTPUT_TO_DELETED_CHANGE
            )
        )

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "DatapointID",
            sectionTitle = "Datapoint IDs",
            sectionDescription = "Added and deleted Datapoint IDs",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                rowid,
                recordIdentityFallback,
                datapointID,
                changeKind,
                dpsFull,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSortBy(datapointID),
                FixedChangeKindSortBy(changeKind)
            ),
            includedChanges = ChangeKind.additionAndDeletion()
        )

        val queryColumnMapping = mapOf(
            "Rowid" to rowid,
            "DPS_Full" to dpsFull,
            "DatapointID" to datapointID
        )

        val partitionCount = PartitionHelper.getPartitionCount(
            sourceDbs,
            "Kenttatunnukset"
        )

        val partitionCriteriaRange = 0.until(partitionCount)

        val queries = partitionCriteriaRange.map { partitionCriteria ->
            """
            SELECT
            rowid AS 'Rowid'
            ,Kenttatunnukset.DPS_Full AS 'DPS_Full'
            ,Kenttatunnukset.DatapointID AS 'DatapointID'

            FROM Kenttatunnukset

            ${if (partitionCount > 1) "WHERE Kenttatunnukset.DatapointID % $partitionCount = $partitionCriteria" else ""}

            """.trimLineStartsAndConsequentBlankLines()
        }

        val sourceTableDescriptors = listOf(
            "Kenttatunnukset"
        )

        return SectionPlanSql.withPartitionedQueries(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            partitionedQueries = queries,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
