package fi.vm.dpm.diff.model

private val ADD_REMOVE_FIELD_KINDS = listOf(
    FieldKind.CORRELATION_KEY,
    FieldKind.IDENTIFICATION_LABEL,
    FieldKind.DIFFERENCE_KIND,
    FieldKind.NOTE
)

data class SourceRecord(
    val sectionFields: List<FieldDescriptor>,
    val fields: Map<FieldDescriptor, String?>
) {
    fun correlationKey(): String {
        val key = fields
            .filter { it.key.fieldKind == FieldKind.CORRELATION_KEY }
            .map { (field, value) ->
                if (value == null && field.fallbackCorrelationKey != null) {
                    fields[field.fallbackCorrelationKey]
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
        differenceFields.fallbackNullCorrelationKeysToNote(sectionFields)
        differenceFields.discardFieldKindsOtherThan(ADD_REMOVE_FIELD_KINDS)

        return DifferenceRecord(
            fields = differenceFields
        )
    }

    fun toRemovedDifference(): DifferenceRecord {
        val differenceFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        differenceFields.setDifferenceKindTo(DifferenceKind.REMOVED, sectionFields)
        differenceFields.fallbackNullCorrelationKeysToNote(sectionFields)
        differenceFields.discardFieldKindsOtherThan(ADD_REMOVE_FIELD_KINDS)

        return DifferenceRecord(
            fields = differenceFields
        )
    }

    fun toChangedDifferenceOrNullFromBaseline(baselineRecord: SourceRecord): DifferenceRecord? {
        val differenceFields: MutableMap<FieldDescriptor, Any?> = fields.toMutableMap()

        differenceFields.setDifferenceKindTo(DifferenceKind.CHANGED, sectionFields)
        differenceFields.fallbackNullCorrelationKeysToNote(sectionFields)
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

private fun MutableMap<FieldDescriptor, Any?>.fallbackNullCorrelationKeysToNote(
    knownFields: List<FieldDescriptor>
) {
    val noteField = knownFields.firstOrNull { it.fieldKind == FieldKind.NOTE } ?: return

    val noteValues = noteValuesForNullCorrelationKeys(this)

    if (noteValues.isEmpty()) return

    val noteValue = noteValues.joinToString(separator = "\n")

    this[noteField] = noteValue
}

private fun noteValuesForNullCorrelationKeys(fields: Map<FieldDescriptor, Any?>): List<String> {

    val composeFields = fields.filter { (field, value) ->
        field.fieldKind == FieldKind.CORRELATION_KEY &&
            value == null &&
            field.fallbackCorrelationNote.any()
    }

    val noteValues = composeFields.map { (field, _) ->
        field.fallbackCorrelationNote.map { "${it.fieldName}: ${fields[it]}" }.joinToString(separator = "\n")
    }

    return noteValues
}

private fun MutableMap<FieldDescriptor, Any?>.compareAndTransformAtomValuesFromBaseline(
    baselineFields: Map<FieldDescriptor, String?>
) {
    val transformedAtoms = filter { (field, _) -> field.fieldKind == FieldKind.ATOM }
        .map { (field, value) ->
            val baselineValue = baselineFields[field]

            if (value == baselineValue) {
                field to null
            } else {
                field to ChangeValue(
                    actualValue = value.toString(),
                    baselineValue = baselineValue
                )
            }
        }

    val (atomsToDiscard, atomsToUpdate) = transformedAtoms.partition { (_, value) -> value == null }
    atomsToDiscard.forEach { (field, _) -> remove(field) }
    atomsToUpdate.forEach { (field, value) -> put(field, value) }
}
