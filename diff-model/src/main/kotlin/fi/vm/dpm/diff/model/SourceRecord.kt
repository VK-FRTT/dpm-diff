package fi.vm.dpm.diff.model

data class SourceRecord(
    val sectionDescriptor: SectionDescriptor,
    val fields: Map<FieldDescriptor, String?>
) {
    companion object {
        private val ADD_REMOVE_FIELD_KINDS = listOf(
            FieldKind.CORRELATION_KEY,
            FieldKind.IDENTIFICATION_LABEL,
            FieldKind.CHANGE_KIND,
            FieldKind.NOTE
        )
    }

    val primaryKey: CorrelationKey by lazy {
        CorrelationKey.primaryKey(this)
    }

    val fullKey: CorrelationKey by lazy {
        CorrelationKey.fullKey(this)
    }

    fun toAddedChange(): ChangeRecord {
        val changeFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        changeFields.setChangeKindTo(ChangeKind.ADDED, sectionDescriptor.sectionFields)
        changeFields.synthesizeNoteField(sectionDescriptor.sectionFields)
        changeFields.discardFieldKindsOtherThan(ADD_REMOVE_FIELD_KINDS)

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toDeletedChange(): ChangeRecord {
        val changeFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        changeFields.setChangeKindTo(ChangeKind.DELETED, sectionDescriptor.sectionFields)
        changeFields.synthesizeNoteField(sectionDescriptor.sectionFields)
        changeFields.discardFieldKindsOtherThan(ADD_REMOVE_FIELD_KINDS)

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toModifiedChangeOrNullFromBaseline(baselineRecord: SourceRecord): ChangeRecord? {
        val changeFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        changeFields.setChangeKindTo(ChangeKind.MODIFIED, sectionDescriptor.sectionFields)
        changeFields.synthesizeNoteField(sectionDescriptor.sectionFields)
        changeFields.compareAndTransformAtomValuesFromBaseline(baselineRecord.fields)

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

private fun MutableMap<FieldDescriptor, Any?>.discardFieldKindsOtherThan(acceptedFieldKinds: List<FieldKind>) {
    val obsoleteFields = filterNot { it.key.fieldKind in acceptedFieldKinds }.keys
    obsoleteFields.forEach { remove(it) }
}

private fun MutableMap<FieldDescriptor, Any?>.synthesizeNoteField(
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

private fun MutableMap<FieldDescriptor, Any?>.compareAndTransformAtomValuesFromBaseline(
    baselineFields: Map<FieldDescriptor, String?>
) {
    val transformedAtoms = this
        .filter { (field, _) ->
            field.fieldKind == FieldKind.ATOM
        }
        .map { (field, value) ->
            value as String?

            val baselineValue = baselineFields[field]

            if (value == baselineValue) {
                field to null
            } else {
                field to ModifiedValue(
                    actualValue = value,
                    baselineValue = baselineValue
                )
            }
        }

    val (atomsToDiscard, atomsToUpdate) = transformedAtoms.partition { (_, value) -> value == null }
    atomsToDiscard.forEach { (field, _) -> remove(field) }
    atomsToUpdate.forEach { (field, value) -> put(field, value) }
}
