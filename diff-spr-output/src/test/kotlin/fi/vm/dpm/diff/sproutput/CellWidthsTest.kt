package fi.vm.dpm.diff.model

import fi.vm.dpm.diff.sproutput.CellWidths
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

internal class CellWidthsTest {

    @Test
    fun `Should not map NO_DISPLAY`() {
        val thrown = catchThrowable {
            CellWidths.widthFromDisplayHint(DisplayHint.NO_DISPLAY) {
                1000.0
            }
        }

        assertThat(thrown).hasMessage("No display, no width")
    }

    @Test
    fun `Should map FIT_BY_TITLE with existingContentWidth 0`() {
        val width = CellWidths.widthFromDisplayHint(DisplayHint.FIT_BY_TITLE) {
            0.0
        }

        assertThat(width).isEqualTo(400)
    }

    @Test
    fun `Should map FIT_BY_TITLE with existingContentWidth 100`() {
        val width = CellWidths.widthFromDisplayHint(DisplayHint.FIT_BY_TITLE) {
            100.0
        }

        assertThat(width).isEqualTo(26000)
    }

    @Test
    fun `Should map FIT_BY_TITLE with existingContentWidth 1000 to max width 65536`() {
        val width = CellWidths.widthFromDisplayHint(DisplayHint.FIT_BY_TITLE) {
            1000.0
        }

        assertThat(width).isEqualTo(65536)
    }

    @Test
    fun `Should map FIXED_WIDE`() {
        val width = CellWidths.widthFromDisplayHint(DisplayHint.FIXED_WIDE) {
            0.0
        }

        assertThat(width).isEqualTo(7500)
    }

    @Test
    fun `Should map FIXED_EXTRA_WIDE`() {
        val width = CellWidths.widthFromDisplayHint(DisplayHint.FIXED_EXTRA_WIDE) {
            0.0
        }

        assertThat(width).isEqualTo(15000)
    }
}
