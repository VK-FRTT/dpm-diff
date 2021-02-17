package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MapExtensionsTest {

    private val keyField1 = KeyField(
        fieldName = "keyField1",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private val keyField2 = KeyField(
        fieldName = "keyField2",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private val atom = AtomField(
        fieldName = "atom"
    )

    private val changeKind = ChangeKindField()

    private val note = NoteField()

    @Test
    fun `filterFieldType should return fields matching given type`() {
        val fields = mapOf<Field, Any?>(
            keyField1 to "keyValue1",
            atom to "atomValue",
            changeKind to ChangeKind.ADDED,
            note to "noteValue",
            keyField2 to "keyValue2"
        )
        val keyFields = fields.filterFieldType<KeyField, Any?>()

        assertThat(keyFields.keys).containsExactly(
            keyField1,
            keyField2
        )

        assertThat(keyFields.values).containsExactly(
            "keyValue1",
            "keyValue2"
        )
    }
}
