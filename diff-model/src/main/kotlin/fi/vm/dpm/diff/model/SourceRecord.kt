package fi.vm.dpm.diff.model

data class SourceRecord(
    val sectionDescriptor: SectionDescriptor,
    val fields: Map<FieldDescriptor, String?>
) {
    val primaryKey: CorrelationKey by lazy {
        CorrelationKey.primaryKey(this)
    }

    val fullKey: CorrelationKey by lazy {
        CorrelationKey.fullKey(this)
    }

    fun toAddedChange(): ChangeRecord {
        val changeFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        changeFields.setChangeKindTo(ChangeKind.ADDED, sectionDescriptor.sectionFields)
        changeFields.generateNoteField(sectionDescriptor.sectionFields)
        changeFields.atomsToAddedChange()
        changeFields.discardFields(
            listOf(
                FieldKind.FALLBACK_VALUE
            )
        )

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toDeletedChange(): ChangeRecord {
        val changeFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        changeFields.setChangeKindTo(ChangeKind.DELETED, sectionDescriptor.sectionFields)
        changeFields.generateNoteField(sectionDescriptor.sectionFields)
        changeFields.discardFields(
            listOf(
                FieldKind.FALLBACK_VALUE,
                FieldKind.ATOM
            )
        )

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toModifiedChangeOrNullFromBaseline(baselineRecord: SourceRecord): ChangeRecord? {
        val changeFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        changeFields.setChangeKindTo(ChangeKind.MODIFIED, sectionDescriptor.sectionFields)
        changeFields.generateNoteField(sectionDescriptor.sectionFields)
        changeFields.atomsToModifiedChange(baselineRecord.fields)
        changeFields.discardFields(
            listOf(
                FieldKind.FALLBACK_VALUE
            )
        )

        val hasAtoms = changeFields.any { (field, _) -> field.fieldKind == FieldKind.ATOM }

        return if (hasAtoms) {
            ChangeRecord(fields = changeFields)
        } else {
            null
        }
    }
}

private fun MutableMap<FieldDescriptor, Any?>.setChangeKindTo(
    changeKind: ChangeKind,
    knownFields: List<FieldDescriptor>
) {
    val changeKindField = knownFields.first { it.fieldKind == FieldKind.CHANGE_KIND }
    this[changeKindField] = changeKind
}

private fun MutableMap<FieldDescriptor, Any?>.generateNoteField(
    knownFields: List<FieldDescriptor>
) {
    val noteField = knownFields.firstOrNull { it.fieldKind == FieldKind.NOTE } ?: return

    val noteValues = composeNoteValuesForNullCorrelationKey(this) +
        composeNoteValuesForBlankIdentificationLabels(this)

    if (noteValues.isEmpty()) return

    val noteValue = noteValues.distinct().joinToString(separator = "\n")

    this[noteField] = noteValue
}

private fun composeNoteValuesForNullCorrelationKey(
    fields: Map<FieldDescriptor, Any?>
): List<String> {

    val nullCorrelationKeyFields = fields.filter { (field, value) ->
        (field.fieldKind == FieldKind.CORRELATION_KEY) && (value == null)
    }

    return composeNoteValues(nullCorrelationKeyFields.keys, fields)
}

private fun composeNoteValuesForBlankIdentificationLabels(
    fields: Map<FieldDescriptor, Any?>
): List<String> {

    val identificationLabels = fields.filter { (field, _) ->
        field.fieldKind == FieldKind.IDENTIFICATION_LABEL
    }

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
    composeFields: Set<FieldDescriptor>,
    fields: Map<FieldDescriptor, Any?>
): List<String> {
    val noteValues = composeFields.flatMap { composeField ->
        composeField.noteFields.map { noteField -> "${noteField.fieldName}: ${fields[noteField]}" }
    }

    return noteValues
}

private fun MutableMap<FieldDescriptor, Any?>.discardFields(discarded: List<FieldKind>) {
    val obsoleteFields = filter { it.key.fieldKind in discarded }.keys
    obsoleteFields.forEach { remove(it) }
}

private fun MutableMap<FieldDescriptor, Any?>.atomsToAddedChange() {
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

private fun MutableMap<FieldDescriptor, Any?>.atomsToModifiedChange(
    baselineFields: Map<FieldDescriptor, String?>
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

private fun <T> MutableMap<FieldDescriptor, Any?>.transformAtoms(transform: (FieldDescriptor, String?) -> T?) {
    val atoms = this
        .filter { (field, _) ->
            field.fieldKind == FieldKind.ATOM
        }
        .map { (field, value) ->
            value as String?

            val modifiedValue = transform(field, value)

            field to modifiedValue
        }

    val (discard, update) = atoms.partition { (_, value) -> value == null }
    discard.forEach { (field, _) -> remove(field) }
    update.forEach { (field, value) -> put(field, value) }
}
