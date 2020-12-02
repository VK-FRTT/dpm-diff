package fi.vm.dpm.diff.repgen.dpm.reportingframework

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

object AxisOrdinateSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        // Scope
        val taxonomyInherentLabel = FallbackField(
            fieldName = "TaxonomyLabel"
        )

        val taxonomyCode = KeySegmentField(
            fieldName = "TaxonomyCode",
            segmentKind = KeySegmentKind.SCOPE_SEGMENT,
            segmentFallback = taxonomyInherentLabel
        )

        val tableInherentLabel = FallbackField(
            fieldName = "TableLabel"
        )

        val tableCode = KeySegmentField(
            fieldName = "TableCode",
            segmentKind = KeySegmentKind.SCOPE_SEGMENT,
            segmentFallback = tableInherentLabel
        )

        val axisInherentLabel = FallbackField(
            fieldName = "AxisLabel"
        )

        val axisOrientation = KeySegmentField(
            fieldName = "AxisOrientation",
            segmentKind = KeySegmentKind.SCOPE_SEGMENT,
            segmentFallback = axisInherentLabel
        )

        // AxisOrdinate
        val ordinateId = FallbackField(
            fieldName = "OrdinateId"
        )

        val ordinateInherentLabel = FallbackField(
            fieldName = "OrdinateLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(ordinateId, ordinateInherentLabel)
        )

        val ordinateCode = KeySegmentField(
            fieldName = "OrdinateCode",
            segmentKind = KeySegmentKind.PRIME_SEGMENT,
            segmentFallback = ordinateInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "OrdinateLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        // Atoms
        val level = AtomField(
            fieldName = "Level"
        )

        val order = AtomField(
            fieldName = "Order"
        )

        val parentOrdinateCode = AtomField(
            fieldName = "ParentOrdinateCode"
        )

        val isDisplayBeforeChildren = AtomField(
            fieldName = "IsDisplayBeforeChildren"
        )

        val isAbstractHeader = AtomField(
            fieldName = "IsAbstractHeader"
        )

        val isRowKey = AtomField(
            fieldName = "IsRowKey"
        )

        val typeOfKey = AtomField(
            fieldName = "TypeOfKey"
        )

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "AxisOrdinates",
            sectionTitle = "AxisOrdinates",
            sectionDescription = "Added and deleted Axis Ordinates, changes in IsDisplayBeforeChildren, IsAbstractHeader, IsRowKey and TypeOfKey",
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
                changeKind,
                level,
                order,
                parentOrdinateCode,
                isDisplayBeforeChildren,
                isAbstractHeader,
                isRowKey,
                typeOfKey,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSort(taxonomyCode),
                NumberAwareSort(tableCode),
                NumberAwareSort(axisOrientation),
                NumberAwareSort(ordinateCode),
                FixedChangeKindSort(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
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
            "Level" to level,
            "Order" to order,
            "ParentOrdinateCode" to parentOrdinateCode,
            "IsDisplayBeforeChildren" to isDisplayBeforeChildren,
            "IsAbstractHeader" to isAbstractHeader,
            "IsRowKey" to isRowKey,
            "TypeOfKey" to typeOfKey
        )

        val query = """
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
             ${identificationLabels.labelAggregateFragment()}
            ,mAxisOrdinate.Level AS 'Level'
            ,mAxisOrdinate.'Order' AS 'Order'
            ,ParentAxisOrdinate.OrdinateCode AS 'ParentOrdinateCode'
            ,mAxisOrdinate.IsDisplayBeforeChildren AS 'IsDisplayBeforeChildren'
            ,mAxisOrdinate.IsAbstractHeader AS 'IsAbstractHeader'
            ,mAxisOrdinate.IsRowKey AS 'IsRowKey'
            ,mAxisOrdinate.TypeOfKey AS 'TypeOfKey'

            FROM mAxisOrdinate
            LEFT JOIN mAxis ON mAxis.AxisID = mAxisOrdinate.AxisID
            LEFT JOIN mTableAxis ON mTableAxis.AxisID = mAxisOrdinate.AxisID
            LEFT JOIN mTable ON mTable.TableID = mTableAxis.TableID
            LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
            LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mAxisOrdinate.ConceptID
            LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID
            LEFT JOIN mAxisOrdinate AS ParentAxisOrdinate ON ParentAxisOrdinate.OrdinateID = mAxisOrdinate.ParentOrdinateID

            WHERE
            (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

            GROUP BY mAxisOrdinate.OrdinateID

            ORDER BY mTaxonomy.TaxonomyCode ASC, mTable.TableCode ASC, mAxis.AxisOrientation ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mAxisOrdinate"
        )

        return SectionPlanSql(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
