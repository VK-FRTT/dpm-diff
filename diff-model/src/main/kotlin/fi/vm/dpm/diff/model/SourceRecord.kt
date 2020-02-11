package fi.vm.dpm.diff.model

data class SourceRecord(
    val sectionFields: List<FieldDescriptor>,
    val fields: Map<FieldDescriptor, String?>
) {
    companion object {
        private val ADD_REMOVE_FIELD_KINDS = listOf(
            FieldKind.CORRELATION_KEY,
            FieldKind.IDENTIFICATION_LABEL,
            FieldKind.DIFFERENCE_KIND,
            FieldKind.NOTE
        )
    }

    fun correlationKey(): String {
        val key = fields
            .filter { it.key.fieldKind == FieldKind.CORRELATION_KEY }
            .map { (field, value) ->
                if (value == null && field.correlationKeyFallback != null) {
                    fields[field.correlationKeyFallback]
                } else {
                    value
                }
            }
            .joinToString(separator = "|")

        return key
    }

    fun toAddedDifference(): DifferenceRecord {
        val differenceFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        differenceFields.setDifferenceKindTo(DifferenceKind.ADDED, sectionFields)
        differenceFields.synthesizeNoteContent(sectionFields)
        differenceFields.discardFieldKindsOtherThan(ADD_REMOVE_FIELD_KINDS)

        return DifferenceRecord(
            fields = differenceFields
        )
    }

    fun toRemovedDifference(): DifferenceRecord {
        val differenceFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        differenceFields.setDifferenceKindTo(DifferenceKind.REMOVED, sectionFields)
        differenceFields.synthesizeNoteContent(sectionFields)
        differenceFields.discardFieldKindsOtherThan(ADD_REMOVE_FIELD_KINDS)

        return DifferenceRecord(
            fields = differenceFields
        )
    }

    fun toChangedDifferenceOrNullFromBaseline(baselineRecord: SourceRecord): DifferenceRecord? {
        val differenceFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        differenceFields.setDifferenceKindTo(DifferenceKind.CHANGED, sectionFields)
        differenceFields.synthesizeNoteContent(sectionFields)
        differenceFields.compareAndTransformAtomValuesFromBaseline(baselineRecord.fields)

        val hasAtoms = differenceFields.any { (field, _) -> field.fieldKind == FieldKind.ATOM }

        return if (hasAtoms) {
            DifferenceRecord(fields = differenceFields)
        } else {
            null
        }
    }
}

private fun MutableMap<FieldDescriptor, Any?>.setDifferenceKindTo(
    differenceKind: DifferenceKind,
    knownFields: List<FieldDescriptor>
) {
    val differenceKindField = knownFields.first { it.fieldKind == FieldKind.DIFFERENCE_KIND }
    this[differenceKindField] = differenceKind
}

private fun MutableMap<FieldDescriptor, Any?>.discardFieldKindsOtherThan(acceptedFieldKinds: List<FieldKind>) {
    val obsoleteFields = filterNot { it.key.fieldKind in acceptedFieldKinds }.keys
    obsoleteFields.forEach { remove(it) }
}

private fun MutableMap<FieldDescriptor, Any?>.synthesizeNoteContent(
    knownFields: List<FieldDescriptor>
) {
    val noteField = knownFields.firstOrNull { it.fieldKind == FieldKind.NOTE } ?: return

    val notes = synthesizeNullCorrelationKeyNotes(this) +
        synthesizeNullIdentificationLabelNotes(this)

    if (notes.isEmpty()) return

    val noteValue = notes.distinct().joinToString(separator = "\n")

    this[noteField] = noteValue
}

private fun synthesizeNullCorrelationKeyNotes(
    fields: Map<FieldDescriptor, Any?>
): List<String> {

    val nullCorrelationKeyFields = fields.filter { (field, value) ->
        field.fieldKind == FieldKind.CORRELATION_KEY &&
            value == null
    }

    val notes = synthesizeNotesForFields(nullCorrelationKeyFields.keys, fields)

    return notes
}

private fun synthesizeNullIdentificationLabelNotes(
    fields: Map<FieldDescriptor, Any?>
): List<String> {

    val identificationLabels = fields.filter { (field, _) ->
        field.fieldKind == FieldKind.CORRELATION_KEY
    }

    val synthesizeNotes = identificationLabels.all { (_, value) ->
        value == null || value.toString().isBlank()
    }

    if (!synthesizeNotes) return emptyList()

    val notes = synthesizeNotesForFields(identificationLabels.keys, fields)

    return notes
}

private fun synthesizeNotesForFields(
    synthesizeFields: Set<FieldDescriptor>,
    fields: Map<FieldDescriptor, Any?>
): List<String> {
    val notes = synthesizeFields.flatMap {
        it.noteFallback.map { "${it.fieldName}: ${fields[it]}" }
    }

    return notes
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
                field to ChangeValue(
                    actualValue = value,
                    baselineValue = baselineValue
                )
            }
        }

    val (atomsToDiscard, atomsToUpdate) = transformedAtoms.partition { (_, value) -> value == null }
    atomsToDiscard.forEach { (field, _) -> remove(field) }
    atomsToUpdate.forEach { (field, value) -> put(field, value) }
}
