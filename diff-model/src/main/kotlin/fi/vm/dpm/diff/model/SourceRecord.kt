package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType
import ext.kotlin.replaceCamelCase
import kotlin.reflect.KClass

data class SourceRecord(
    val sectionDescriptor: SectionDescriptor,
    val sourceKind: SourceKind,
    val fields: Map<Field, String?>
) {
    val objectKey: CorrelationKey by lazy {
        CorrelationKey.objectKey(this)
    }

    val fullKey: CorrelationKey by lazy {
        CorrelationKey.fullKey(this)
    }

    fun toAddedChange(): ChangeRecord {
        val changeFields: MutableMap<Field, Any?> = fields.toMutableMap()

        changeFields.transformAtomsToAddedChange()
        changeFields.setChangeKind(ChangeKind.ADDED)
        changeFields.setNoteWithDetails(
            recordIdentificationDetailIfNeeded = true
        )

        changeFields.discardFields(
            listOf(
                FallbackField::class
            )
        )

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toDeletedChange(): ChangeRecord {
        val changeFields: MutableMap<Field, Any?> = fields.toMutableMap()

        changeFields.setChangeKind(ChangeKind.DELETED)
        changeFields.setNoteWithDetails(
            recordIdentificationDetailIfNeeded = true
        )

        changeFields.discardFields(
            listOf(
                FallbackField::class,
                AtomField::class
            )
        )

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toDuplicateKeyChange(): ChangeRecord {
        val changeFields: MutableMap<Field, Any?> = fields.toMutableMap()

        changeFields.setChangeKind(ChangeKind.DUPLICATE_KEY)
        changeFields.setNoteWithDetails(
            recordIdentificationDetailIfNeeded = true
        )

        changeFields.discardFields(
            listOf(
                FallbackField::class,
                AtomField::class
            )
        )

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toModifiedChangeOrNullFromBaseline(baselineRecord: SourceRecord): ChangeRecord? {
        val changeFields: MutableMap<Field, Any?> = fields.toMutableMap()

        changeFields.transformAtomsToModifiedChange(baselineRecord.fields)
        changeFields.setChangeKind(ChangeKind.MODIFIED)
        changeFields.setNoteWithDetails(
            recordIdentificationDetailIfNeeded = true,
            modifiedChangeAtomFieldDetail = true
        )

        changeFields.discardFields(
            listOf(
                FallbackField::class
            )
        )

        val atoms = changeFields.filterFieldType<AtomField, Any?>()

        return if (atoms.isNotEmpty()) {
            ChangeRecord(fields = changeFields)
        } else {
            null
        }
    }

    private inline fun <reified FT : Field> optionalFieldOfType(): FT? {
        val classCriteria = FT::class

        val fields = sectionDescriptor.sectionFields.filter { it::class == classCriteria }

        if (fields.isEmpty()) return null
        return fields.first() as FT
    }

    private inline fun <reified FT : Field> requiredFieldOfType(): FT {
        return optionalFieldOfType() ?: thisShouldNeverHappen("No field with type: ${FT::class.simpleName}")
    }

    private fun MutableMap<Field, Any?>.transformAtomsToAddedChange() {
        doTransformAtoms { field, value ->

            if (field.atomOptions == AtomOption.OUTPUT_TO_ADDED_CHANGE) {
                AddedChangeAtomValue(
                    value = value
                )
            } else {
                null
            }
        }
    }

    private fun MutableMap<Field, Any?>.transformAtomsToModifiedChange(
        baselineFields: Map<Field, String?>
    ) {
        doTransformAtoms { field, value ->
            val baselineValue = baselineFields[field]

            if (value != baselineValue) {
                ModifiedChangeAtomValue(
                    currentValue = value,
                    baselineValue = baselineValue
                )
            } else {
                null
            }
        }
    }

    private fun <T> MutableMap<Field, Any?>.doTransformAtoms(transform: (AtomField, String?) -> T?) {
        val atoms = this
            .filterFieldType<AtomField, Any?>()
            .map { (field, value) ->
                value as String?

                val modifiedValue = transform(field, value)

                field to modifiedValue
            }

        val (discard, update) = atoms.partition { (_, value) -> value == null }
        discard.forEach { (field, _) -> remove(field) }
        update.forEach { (field, value) -> put(field, value) }
    }

    private fun MutableMap<Field, Any?>.setChangeKind(
        changeKind: ChangeKind
    ) {
        val changeKindField = requiredFieldOfType<ChangeKindField>()
        this[changeKindField] = changeKind
    }

    private fun MutableMap<Field, Any?>.setNoteWithDetails(
        recordIdentificationDetailIfNeeded: Boolean = false,
        recordIdentificationDetailAlways: Boolean = false,
        modifiedChangeAtomFieldDetail: Boolean = false
    ) {
        val details = listOf(
            {
                if (recordIdentificationDetailIfNeeded) {
                    recordIdentificationDetail(this, false)
                } else {
                    null
                }
            },
            {
                if (recordIdentificationDetailAlways) {
                    recordIdentificationDetail(this, true)
                } else {
                    null
                }
            },
            {
                if (modifiedChangeAtomFieldDetail) {
                    modifiedChangeAtomFieldDetail(this)
                } else {
                    null
                }
            }
        ).mapNotNull { it() }

        if (details.isEmpty()) return

        val noteField = requiredFieldOfType<NoteField>()
        val noteValue = details.joinToString(separator = "\n\n")

        this[noteField] = noteValue
    }

    private fun MutableMap<Field, Any?>.discardFields(discarded: List<KClass<*>>) {
        val discard = filter { it::class in discarded }.keys
        discard.forEach { remove(it) }
    }

    private fun recordIdentificationDetail(
        fields: Map<Field, Any?>,
        alwaysOutputRecordIdentificationDetail: Boolean
    ): String? {
        fun shouldOutputRecordIdentityFallbackForCorrelationKeys() = fields
            .filterFieldType<KeySegmentField, Any?>()
            .filter { (field, value) -> field.shouldOutputRecordIdentityFallback(value) }
            .any()

        fun shouldOutputRecordIdentityFallbackForIdentificationLabels() = fields
            .filterFieldType<IdentificationLabelField, Any?>()
            .all { (field, value) -> field.shouldOutputRecordIdentityFallback(value) }

        val identityFallbackField = optionalFieldOfType<RecordIdentityFallbackField>() ?: return null

        return if (
            alwaysOutputRecordIdentificationDetail ||
            shouldOutputRecordIdentityFallbackForCorrelationKeys() ||
            shouldOutputRecordIdentityFallbackForIdentificationLabels()
        ) {
            val identityFallbackItems = identityFallbackField
                .identityFallbacks
                .map { fallbackField -> "${fallbackField.fieldName.replaceCamelCase()}: ${fields[fallbackField]}" }

            layoutNoteDetail(
                detailTitle = "$sourceKind ${identityFallbackField.fieldName.replaceCamelCase().toUpperCase()}",
                detailItems = identityFallbackItems
            )
        } else {
            null
        }
    }

    private fun modifiedChangeAtomFieldDetail(
        fields: Map<Field, Any?>
    ): String? {

        val modifiedFieldItems = fields
            .filterFieldType<AtomField, Any?>()
            .filter { (_, value) -> value is ModifiedChangeAtomValue }
            .map { (field, _) -> field.fieldName.replaceCamelCase() }

        return layoutNoteDetail(
            detailTitle = "MODIFIED",
            detailItems = modifiedFieldItems
        )
    }

    private fun layoutNoteDetail(
        detailTitle: String,
        detailItems: List<String>
    ): String? {
        if (detailItems.isEmpty()) return null
        val itemPrefix = "\n- "
        return "$detailTitle:${detailItems.joinToString(prefix = itemPrefix, separator = itemPrefix)}"
    }
}
