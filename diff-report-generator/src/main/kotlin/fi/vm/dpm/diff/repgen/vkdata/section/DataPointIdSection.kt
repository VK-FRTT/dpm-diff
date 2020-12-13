package fi.vm.dpm.diff.repgen.vkdata.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.AtomOption
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

object DataPointIdSection {

    fun sectionPlan(): SectionPlanSql {

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

        val openContext = AtomField(
            fieldName = "OpenContext",
            atomOptions = listOf(
                AtomOption.OUTPUT_TO_ADDED_CHANGE,
                AtomOption.OUTPUT_TO_DELETED_CHANGE
            )
        )

        val dps = AtomField(
            fieldName = "Dps",
            atomOptions = listOf(
                AtomOption.OUTPUT_TO_ADDED_CHANGE,
                AtomOption.OUTPUT_TO_DELETED_CHANGE
            )
        )

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "DatapointIDs",
            sectionTitle = "Datapoint IDs",
            sectionDescription = "Added and deleted Datapoint IDs",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_FIRST_BY_KEY_FIELDS_AND_THEN_BY_ATOMS,
            sectionFields = listOf(
                rowid,
                recordIdentityFallback,
                datapointID,
                changeKind,
                dps,
                dpsFull,
                openContext,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSort(datapointID),
                FixedChangeKindSort(changeKind)
            ),
            includedChanges = ChangeKind.additionAndDeletion()
        )

        val queryColumnMapping = mapOf(
            "Rowid" to rowid,
            "DPS" to dps,
            "DPS_Full" to dpsFull,
            "DatapointID" to datapointID,
            "OpenContext" to openContext
        )

        val queries = (0..9).map { remainder ->
            """
            SELECT
            rowid AS 'Rowid'
            ,Kenttatunnukset.DPS AS 'DPS'
            ,Kenttatunnukset.DPS_Full AS 'DPS_Full'
            ,Kenttatunnukset.DatapointID AS 'DatapointID'
            ,Kenttatunnukset.OpenContext AS 'OpenContext'

            FROM Kenttatunnukset

            WHERE Kenttatunnukset.DatapointID % 10 = $remainder

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
