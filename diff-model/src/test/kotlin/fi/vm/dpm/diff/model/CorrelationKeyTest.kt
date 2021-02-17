package fi.vm.dpm.diff.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

internal class CorrelationKeyTest {

    private val keyField = KeyField(
        fieldName = "keyField",
        keyFieldKind = KeyFieldKind.PRIME_KEY,
        keyFieldFallback = null
    )

    private val fieldsA = mapOf<Field, Any?>(
        keyField to "keyValueA"
    )

    private val fieldsB = mapOf<Field, Any?>(
        keyField to "keyValueB"
    )

    @Test
    fun `equals should properly compare CorrelationKeys`() {
        val keyA1 = CorrelationKey.createCorrelationKey(
            CorrelationKeyKind.FULL_KEY_FIELD_CORRELATION_KEY,
            fieldsA
        )

        val keyA2 = CorrelationKey.createCorrelationKey(
            CorrelationKeyKind.FULL_KEY_FIELD_CORRELATION_KEY,
            fieldsA
        )

        val keyB = CorrelationKey.createCorrelationKey(
            CorrelationKeyKind.FULL_KEY_FIELD_CORRELATION_KEY,
            fieldsB
        )

        assertThat(keyA1 == keyA1).isTrue()
        assertThat(keyA1 == keyA2).isTrue()
        assertThat(keyA1 == keyB).isFalse()
        assertThat(keyA1.equals(this)).isFalse()
    }

    @Test
    fun `equals should fail when different kind CorrelationKeys are compared`() {
        val keyFull = CorrelationKey.createCorrelationKey(
            CorrelationKeyKind.FULL_KEY_FIELD_CORRELATION_KEY,
            fieldsA
        )

        val keyParent = CorrelationKey.createCorrelationKey(
            CorrelationKeyKind.PARENT_KEY_FIELD_CORRELATION_KEY,
            fieldsA
        )

        val thrown = catchThrowable {
            if (keyFull == keyParent) {
                thisShouldNeverHappen("Previous comparison should have failed.")
            }
        }

        assertThat(thrown).hasMessage("Check failed.")
        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
    }
}
