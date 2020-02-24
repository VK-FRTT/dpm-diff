package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType
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

        changeFields.setChangeKindTo(ChangeKind.ADDED, sectionDescriptor.sectionFields)
        changeFields.generateNoteField(sectionDescriptor.sectionFields)
        changeFields.atomsToAddedChange()
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

        changeFields.setChangeKindTo(ChangeKind.DELETED, sectionDescriptor.sectionFields)
        changeFields.generateNoteField(sectionDescriptor.sectionFields)
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

        changeFields.setChangeKindTo(ChangeKind.MODIFIED, sectionDescriptor.sectionFields)
        changeFields.generateNoteField(sectionDescriptor.sectionFields)
        changeFields.atomsToModifiedChange(baselineRecord.fields)
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

private fun MutableMap<Field, Any?>.setChangeKindTo(
    changeKind: ChangeKind,
    knownFields: List<Field>
) {
    val changeKindField = knownFields.filterFieldType<Field, ChangeKindField>().first()
    this[changeKindField] = changeKind
}

private fun MutableMap<Field, Any?>.generateNoteField(
    knownFields: List<Field>
) {
    val noteField = knownFields.filterFieldType<Field, NoteField>().firstOrNull() ?: return

    val noteValues = composeNoteValuesForNullCorrelationKey(this) +
        composeNoteValuesForBlankIdentificationLabels(this)

    if (noteValues.isEmpty()) return

    val noteValue = noteValues.distinct().joinToString(separator = "\n")

    this[noteField] = noteValue
}

private fun composeNoteValuesForNullCorrelationKey(
    fields: Map<Field, Any?>
): List<String> {

    val nullCorrelationKeyFields = fields
        .filterFieldType<Field, Any?, CorrelationKeyField>()
        .filter { (_, value) ->
            value == null
        }

    return composeNoteValues(nullCorrelationKeyFields.keys, fields)
}

private fun composeNoteValuesForBlankIdentificationLabels(
    fields: Map<Field, Any?>
): List<String> {

    val identificationLabels = fields
        .filterFieldType<Field, Any?, IdentificationLabelField>()

    val allIdLabelsNullOrBlank = identificationLabels.all { (_, value) ->
        value == null || value.toString().isBlank()
    }

    return if (allIdLabelsNullOrBlank) {
        composeNoteValues(identificationLabels.keys, fields)
    } else {
        emptyList()
    }
}

private fun composeNoteValues(
    composeFields: Set<Field>,
    fields: Map<Field, Any?>
): List<String> {
    fun composeNoteForFallback(fallbackField: FallbackField) = "${fallbackField.fieldName}: ${fields[fallbackField]}"

    val noteValues = composeFields.flatMap { composeField ->
        when (composeField) {
            is CorrelationKeyField -> composeField.noteFallbacks.map { composeNoteForFallback(it) }
            is IdentificationLabelField -> composeField.noteFallbacks.map { composeNoteForFallback(it) }
            else -> emptyList()
        }
    }

    return noteValues
}

private fun MutableMap<Field, Any?>.discardFields(discarded: List<KClass<*>>) {
    val discard = filter { it::class in discarded }.keys
    discard.forEach { remove(it) }
}

private fun MutableMap<Field, Any?>.atomsToAddedChange() {
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

private fun MutableMap<Field, Any?>.atomsToModifiedChange(
    baselineFields: Map<Field, String?>
) {
    transformAtoms { field, value ->
        val baselineValue = baselineFields[field]

        if (value != baselineValue) {
            ModifiedChangeAtomValue(
                actualValue = value,
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
