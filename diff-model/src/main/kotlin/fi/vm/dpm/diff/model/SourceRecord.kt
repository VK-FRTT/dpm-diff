package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType
import ext.kotlin.replaceCamelCase
import kotlin.reflect.KClass

data class SourceRecord(
    val sectionDescriptor: SectionDescriptor,
    val fields: Map<Field, String?>
) {
    val primaryKey: CorrelationKey by lazy {
        CorrelationKey.primaryKey(this)
    }

    val fullKey: CorrelationKey by lazy {
        CorrelationKey.fullKey(this)
    }

    fun toAddedChange(): ChangeRecord {
        val changeFields: MutableMap<Field, Any?> = fields.toMutableMap()

        changeFields.transformAtomsToAddedChange()
        changeFields.setChangeKind(sectionDescriptor.sectionFields, ChangeKind.ADDED)
        changeFields.setNoteWithDetails(sectionDescriptor.sectionFields) {
            nullCorrelationKeyNoteDetails(it) + blankIdentificationLabelNoteDetails(it)
        }

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

        changeFields.setChangeKind(sectionDescriptor.sectionFields, ChangeKind.DELETED)
        changeFields.setNoteWithDetails(sectionDescriptor.sectionFields) {
            nullCorrelationKeyNoteDetails(it) + blankIdentificationLabelNoteDetails(it)
        }

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
        changeFields.setChangeKind(sectionDescriptor.sectionFields, ChangeKind.MODIFIED)
        changeFields.setNoteWithDetails(sectionDescriptor.sectionFields) {
            nullCorrelationKeyNoteDetails(it) +
                blankIdentificationLabelNoteDetails(it) +
                modifiedChangeAtomFieldNoteDetails(it)
        }

        changeFields.discardFields(
            listOf(
                FallbackField::class
            )
        )

        val atoms = changeFields.filterFieldType<Field, Any?, AtomField>()

        return if (atoms.isNotEmpty()) {
            ChangeRecord(fields = changeFields)
        } else {
            null
        }
    }
}

private fun MutableMap<Field, Any?>.setChangeKind(
    knownFields: List<Field>,
    changeKind: ChangeKind
) {
    val changeKindField = knownFields.filterFieldType<Field, ChangeKindField>().first()
    this[changeKindField] = changeKind
}

private fun MutableMap<Field, Any?>.setNoteWithDetails(
    knownFields: List<Field>,
    detailsComposer: (MutableMap<Field, Any?>) -> List<String>
) {
    val noteField = knownFields.filterFieldType<Field, NoteField>().first()

    val details = detailsComposer(this)

    if (details.isEmpty()) return

    val noteValue = details.distinct().joinToString(separator = "\n")

    this[noteField] = noteValue
}

private fun nullCorrelationKeyNoteDetails(
    fields: Map<Field, Any?>
): List<String> {

    val nullCorrelationKeyFields = fields
        .filterFieldType<Field, Any?, CorrelationKeyField>()
        .filter { (_, value) ->
            value == null
        }

    return noteFallbacksToNoteDetails(nullCorrelationKeyFields.keys, fields)
}

private fun blankIdentificationLabelNoteDetails(
    fields: Map<Field, Any?>
): List<String> {

    val identificationLabels = fields
        .filterFieldType<Field, Any?, IdentificationLabelField>()

    val allIdLabelsNullOrBlank = identificationLabels.all { (_, value) ->
        value == null || value.toString().isBlank()
    }

    return if (allIdLabelsNullOrBlank) {
        noteFallbacksToNoteDetails(identificationLabels.keys, fields)
    } else {
        emptyList()
    }
}

private fun noteFallbacksToNoteDetails(
    composeFields: Set<Field>,
    fields: Map<Field, Any?>
): List<String> {
    fun FallbackField.composeNoteDetail() = "$fieldName: ${fields[this]}"

    val noteValues = composeFields.flatMap { composeField ->
        when (composeField) {
            is CorrelationKeyField -> composeField.noteFallbacks.map { it.composeNoteDetail() }
            is IdentificationLabelField -> composeField.noteFallbacks.map { it.composeNoteDetail() }
            else -> emptyList()
        }
    }

    return noteValues
}

private fun modifiedChangeAtomFieldNoteDetails(
    fields: Map<Field, Any?>
): List<String> {

    val modifiedFields = fields
        .filterFieldType<Field, Any?, AtomField>()
        .filter { (_, value) -> value is ModifiedChangeAtomValue }
        .map { (field, _) -> field.fieldName.replaceCamelCase() }

    return if (modifiedFields.isNotEmpty()) {
        listOf(
            "Modifications:${modifiedFields.joinToString(prefix = "\n- ", separator = "\n- ")}"
        )
    } else {
        emptyList()
    }
}

private fun MutableMap<Field, Any?>.discardFields(discarded: List<KClass<*>>) {
    val discard = filter { it::class in discarded }.keys
    discard.forEach { remove(it) }
}

private fun MutableMap<Field, Any?>.transformAtomsToAddedChange() {
    transformAtoms { field, value ->

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
    transformAtoms { field, value ->
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

private fun <T> MutableMap<Field, Any?>.transformAtoms(transform: (AtomField, String?) -> T?) {
    val atoms = this
        .filterFieldType<Field, Any?, AtomField>()
        .map { (field, value) ->
            value as String?

            val modifiedValue = transform(field, value)

            field to modifiedValue
        }

    val (discard, update) = atoms.partition { (_, value) -> value == null }
    discard.forEach { (field, _) -> remove(field) }
    update.forEach { (field, value) -> put(field, value) }
}
