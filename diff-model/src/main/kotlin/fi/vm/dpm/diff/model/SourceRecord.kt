package fi.vm.dpm.diff.model

private val ADD_REMOVE_DIFFERENCE_FIELD_KINDS = listOf(
    FieldKind.CORRELATION_ID,
    FieldKind.DISCRIMINATION_LABEL,
    FieldKind.DIFFERENCE_KIND
)

data class SourceRecord(
    val fields: Map<FieldDescriptor, String?>
) {
    fun correlationKey(): String {
        return fields
            .filter { it.key.fieldKind == FieldKind.CORRELATION_ID }
            .map { it.value }
            .joinToString(separator = "|")
    }

    fun toAddedDifference(): DifferenceRecord {
        return toAddRemoveDifference(DifferenceKind.ADDED)
    }

    fun toRemovedDifference(): DifferenceRecord {
        return toAddRemoveDifference(DifferenceKind.REMOVED)
    }

    fun toChangedDifferenceOrNull(baselineRecord: SourceRecord): DifferenceRecord? {
        val differenceFields = fields.mapNotNull { (field, value) ->
            when (field.fieldKind) {
                FieldKind.CORRELATION_ID -> field to value
                FieldKind.DISCRIMINATION_LABEL -> field to value
                FieldKind.DIFFERENCE_KIND -> field to DifferenceKind.CHANGED
                FieldKind.CHANGE -> {
                    val baselineValue = baselineRecord.fields[field]

                    if (value != baselineValue) {
                        field to ChangeValue(
                            actualValue = value,
                            baselineValue = baselineValue
                        )
                    } else {
                        null
                    }
                }
            }
        }.toMap()

        val hasChangeFields = differenceFields.any { (field, _) -> field.fieldKind == FieldKind.CHANGE }

        return if (hasChangeFields) {
            DifferenceRecord(
                fields = differenceFields
            )
        } else {
            null
        }
    }

    private fun toAddRemoveDifference(
        differenceKind: DifferenceKind
    ): DifferenceRecord {
        val differenceFields = fields
            .filter { it.key.fieldKind in ADD_REMOVE_DIFFERENCE_FIELD_KINDS }
            .map { (field, value) ->
                if (field.fieldKind == FieldKind.DIFFERENCE_KIND) {
                    field to differenceKind
                } else {
                    field to value
                }
            }
            .toMap()

        return DifferenceRecord(
            fields = differenceFields
        )
    }
}
