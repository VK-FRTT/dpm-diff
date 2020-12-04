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

object HierarchyNodeStructureSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val hierarchyId = FallbackField(
            fieldName = "HierarchyId"
        )

        val hierarchyInherentLabel = FallbackField(
            fieldName = "HierarchyLabel"
        )

        val memberId = FallbackField(
            fieldName = "MemberId"
        )

        val memberInherentLabel = FallbackField(
            fieldName = "MemberLabel"
        )

        val hierarchyNodeInherentLabel = FallbackField(
            fieldName = "HierarchyNodeLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(hierarchyId, memberId, hierarchyNodeInherentLabel)
        )

        val hierarchyCode = KeySegmentField(
            fieldName = "HierarchyCode",
            segmentKind = KeySegmentKind.PRIME_SEGMENT,
            segmentFallback = hierarchyInherentLabel
        )

        val memberCode = KeySegmentField(
            fieldName = "MemberCode",
            segmentKind = KeySegmentKind.PRIME_SEGMENT,
            segmentFallback = memberInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "HierarchyNodeLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        val parentMemberCode = AtomField(
            fieldName = "ParentMemberCode"
        )

        val order = AtomField(
            fieldName = "Order"
        )

        val level = AtomField(
            fieldName = "Level"
        )
        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "HierNodeStructure",
            sectionTitle = "HierarchyNodes structure",
            sectionDescription = "Added and deleted HierarchyNodes, changes in Parent Member, Order and Level details",
            sectionFields = listOf(
                hierarchyId,
                hierarchyInherentLabel,
                memberId,
                memberInherentLabel,
                hierarchyNodeInherentLabel,
                recordIdentityFallback,
                hierarchyCode,
                memberCode,
                *identificationLabels.labelFields(),
                changeKind,
                parentMemberCode,
                order,
                level,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSort(hierarchyCode),
                NumberAwareSort(memberCode),
                FixedChangeKindSort(changeKind)
            ),

            // Report all changes (i.e. also adds/deletes) as those explain why
            // subsequent nodes order/level values change
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "HierarchyId" to hierarchyId,
            "HierarchyInherentLabel" to hierarchyInherentLabel,
            "MemberId" to memberId,
            "MemberInherentLabel" to memberInherentLabel,
            "HierarchyNodeInherentLabel" to hierarchyNodeInherentLabel,
            "HierarchyCode" to hierarchyCode,
            "MemberCode" to memberCode,
            *identificationLabels.labelColumnMapping(),
            "ParentMemberCode" to parentMemberCode,
            "Order" to order,
            "Level" to level
        )

        val query = """
            SELECT
            mHierarchyNode.HierarchyID AS 'HierarchyId'
            ,mHierarchy.HierarchyLabel AS 'HierarchyInherentLabel'
            ,mHierarchyNode.MemberID AS 'MemberId'
            ,mMember.MemberLabel AS 'MemberInherentLabel'
            ,mHierarchyNode.HierarchyNodeLabel AS 'HierarchyNodeInherentLabel'
            ,mHierarchy.HierarchyCode AS 'HierarchyCode'
            ,mMember.MemberCode AS 'MemberCode'
             ${identificationLabels.labelAggregateFragment()}
            ,ParentMember.MemberCode AS 'ParentMemberCode'
            ,mHierarchyNode.'Order' AS 'Order'
            ,mHierarchyNode.Level AS 'Level'

            FROM mHierarchyNode
            LEFT JOIN mHierarchy ON mHierarchy.HierarchyID = mHierarchyNode.HierarchyID
            LEFT JOIN mMember ON mMember.MemberID = mHierarchyNode.MemberID
            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mHierarchyNode.ConceptID
            LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID
            LEFT JOIN mMember AS ParentMember ON ParentMember.MemberID = mHierarchyNode.ParentMemberID

            WHERE
            (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

            GROUP BY mHierarchyNode.HierarchyID, mHierarchyNode.MemberID

            ORDER BY mHierarchy.HierarchyCode ASC, mHierarchyNode.'Order' ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mHierarchyNode"
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
