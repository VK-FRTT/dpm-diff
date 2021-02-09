package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.sproutput.ColumnDescriptor
import fi.vm.dpm.diff.sproutput.SectionSheetColumns
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SectionSheetColumnsTest {

    private val fallbackField = FallbackField(
        fieldName = "fallbackField"
    )

    private val recordIdentityFallbackField = RecordIdentityFallbackField(
        identityFallbacks = listOf(fallbackField)
    )

    private val contextParentKeyField = KeyField(
        fieldName = "contextParentKeyField",
        keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
        keyFieldFallback = null
    )

    private val primeKeyField = KeyField(
        fieldName = "primeKeyField",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private val identificationLabelField = IdentificationLabelField(
        fieldName = "identificationLabelField"
    )

    private val changeKindField = ChangeKindField()

    private val atomField = AtomField(
        fieldName = "atomField"
    )

    private val noteField = NoteField()

    @Test
    fun `Should not map FallbackField to column`() {
        val fields = listOf(fallbackField)
        val columns = SectionSheetColumns.mapFieldsToColumns(fields)
        assertThat(columns).isEmpty()
    }

    @Test
    fun `Should not map RecordIdentityFallbackField to column`() {
        val fields = listOf(recordIdentityFallbackField)
        val columns = SectionSheetColumns.mapFieldsToColumns(fields)
        assertThat(columns).isEmpty()
    }

    @Test
    fun `Should map contextParentKeyField to column`() {
        val fields = listOf(contextParentKeyField)
        val columns = SectionSheetColumns.mapFieldsToColumns(fields)

        assertThat(columns).size().isEqualTo(1)

        val column = columns[0]

        assertThat(column.transposeToConfigList()).containsExactly(
            "Field: KeyField (contextParentKeyField)",
            "Field.displayHint: FIT_BY_TITLE",
            "ColumnTitle: contextParentKeyField",
            "HeaderStyle: HEADER_STYLE_DIMMED",
            "ContentStyle: CONTENT_STYLE_DIMMED"
        )

        assertThat(column.mapChangeValueToCell("input")).isEqualTo("input")
    }

    @Test
    fun `Should map primeKeyField to column`() {
        val fields = listOf(primeKeyField)
        val columns = SectionSheetColumns.mapFieldsToColumns(fields)

        assertThat(columns).size().isEqualTo(1)

        val column = columns[0]

        assertThat(column.transposeToConfigList()).containsExactly(
            "Field: KeyField (primeKeyField)",
            "Field.displayHint: FIT_BY_TITLE",
            "ColumnTitle: primeKeyField",
            "HeaderStyle: HEADER_STYLE_NORMAL",
            "ContentStyle: CONTENT_STYLE_NORMAL"
        )

        assertThat(column.mapChangeValueToCell("input")).isEqualTo("input")
    }

    @Test
    fun `Should map identificationLabelField to column`() {
        val fields = listOf(identificationLabelField)
        val columns = SectionSheetColumns.mapFieldsToColumns(fields)

        assertThat(columns).size().isEqualTo(1)

        val column = columns[0]

        assertThat(column.transposeToConfigList()).containsExactly(
            "Field: IdentificationLabelField (identificationLabelField)",
            "Field.displayHint: FIT_BY_TITLE",
            "ColumnTitle: identificationLabelField",
            "HeaderStyle: HEADER_STYLE_NORMAL",
            "ContentStyle: CONTENT_STYLE_NORMAL"
        )

        assertThat(column.mapChangeValueToCell("input")).isEqualTo("input")
    }

    @Test
    fun `Should map changeKindField to column`() {
        val fields = listOf(changeKindField)
        val columns = SectionSheetColumns.mapFieldsToColumns(fields)

        assertThat(columns).size().isEqualTo(1)

        val column = columns[0]

        assertThat(column.transposeToConfigList()).containsExactly(
            "Field: ChangeKindField (Change)",
            "Field.displayHint: FIT_BY_TITLE",
            "ColumnTitle: Change",
            "HeaderStyle: HEADER_STYLE_NORMAL",
            "ContentStyle: CONTENT_STYLE_NORMAL"
        )

        assertThat(column.mapChangeValueToCell(ChangeKind.ADDED)).isEqualTo("ADDED")
        assertThat(column.mapChangeValueToCell(ChangeKind.DELETED)).isEqualTo("DELETED")
        assertThat(column.mapChangeValueToCell(ChangeKind.MODIFIED)).isEqualTo("MODIFIED")
        assertThat(column.mapChangeValueToCell(ChangeKind.DUPLICATE_KEY_ALERT)).isEqualTo("DUPLICATE_KEY_ALERT")
    }

    @Test
    fun `Should map atomField to two columns, first is current`() {
        val fields = listOf(atomField)
        val columns = SectionSheetColumns.mapFieldsToColumns(fields)

        assertThat(columns).size().isEqualTo(2)

        val currentColumn = columns[0]

        assertThat(currentColumn.transposeToConfigList()).containsExactly(
            "Field: AtomField (atomField)",
            "Field.displayHint: FIT_BY_TITLE",
            "ColumnTitle: atomField",
            "HeaderStyle: HEADER_STYLE_NORMAL",
            "ContentStyle: CONTENT_STYLE_NORMAL"
        )

        assertThat(
            currentColumn.mapChangeValueToCell(
                ChangeAtomValueAdded(value = "added-value")
            )
        ).isEqualTo("added-value")

        assertThat(
            currentColumn.mapChangeValueToCell(
                ChangeAtomValueDeleted(value = "deleted-value")
            )
        ).isNull()

        assertThat(
            currentColumn.mapChangeValueToCell(
                ChangeAtomValueModified(
                    currentValue = "current-value",
                    baselineValue = "baseline-value")
            )
        ).isEqualTo("current-value")

        assertThat(
            currentColumn.mapChangeValueToCell(
                "input"
            )
        ).isNull()
    }

    @Test
    fun `Should map atomField to two columns, second is baseline`() {
        val fields = listOf(atomField)
        val columns = SectionSheetColumns.mapFieldsToColumns(fields)

        assertThat(columns).size().isEqualTo(2)

        val baselineColumn = columns[1]

        assertThat(baselineColumn.transposeToConfigList()).containsExactly(
            "Field: AtomField (atomField)",
            "Field.displayHint: FIT_BY_TITLE",
            "ColumnTitle: atomField (baseline)",
            "HeaderStyle: HEADER_STYLE_NORMAL",
            "ContentStyle: CONTENT_STYLE_NORMAL"
        )

        assertThat(
            baselineColumn.mapChangeValueToCell(
                ChangeAtomValueAdded(value = "added-value")
            )
        ).isNull()

        assertThat(
            baselineColumn.mapChangeValueToCell(
                ChangeAtomValueDeleted(value = "deleted-value")
            )
        ).isEqualTo("deleted-value")

        assertThat(
            baselineColumn.mapChangeValueToCell(
                ChangeAtomValueModified(
                    currentValue = "current-value",
                    baselineValue = "baseline-value")
            )
        ).isEqualTo("baseline-value")

        assertThat(
            baselineColumn.mapChangeValueToCell(
                "input"
            )
        ).isNull()
    }

    @Test
    fun `Should map noteField to column`() {
        val fields = listOf(noteField)
        val columns = SectionSheetColumns.mapFieldsToColumns(fields)

        assertThat(columns).size().isEqualTo(1)

        val column = columns[0]

        assertThat(column.transposeToConfigList()).containsExactly(
            "Field: NoteField (Notes)",
            "Field.displayHint: FIXED_WIDE",
            "ColumnTitle: Notes",
            "HeaderStyle: HEADER_STYLE_NORMAL",
            "ContentStyle: CONTENT_STYLE_NORMAL"
        )

        assertThat(column.mapChangeValueToCell("input")).isEqualTo("input")
    }

    private fun ColumnDescriptor.transposeToConfigList(): List<String> =
        listOf(
            "Field: ${field.javaClass.simpleName} (${field.fieldName})",
            "Field.displayHint: ${field.displayHint}",
            "ColumnTitle: $columnTitle",
            "HeaderStyle: $headerStyle",
            "ContentStyle: $contentStyle"
        )
}
