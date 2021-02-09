package fi.vm.dpm.diff.sproutput

import fi.vm.dpm.diff.model.DisplayHint
import fi.vm.dpm.diff.model.thisShouldNeverHappen

object CellWidths {

    private const val FIT_BY_TITLE_MARGIN = 400
    private const val FIXED_WIDE_WIDTH = 7500
    private const val FIXED_EXTRA_WIDE_WIDTH = 15000

    fun widthFromDisplayHint(
        displayHint: DisplayHint,
        getExistingContentWidth: () -> (Double)
    ): Int {
        return when (displayHint) {
            DisplayHint.NO_DISPLAY -> {
                thisShouldNeverHappen("No display, no width")
            }

            DisplayHint.FIT_BY_TITLE -> {
                getExistingContentWidth()
                    .let { it * 256 + FIT_BY_TITLE_MARGIN }
                    .toInt()
                    .coerceIn(0..256 * 256)
            }

            DisplayHint.FIXED_WIDE -> {
                FIXED_WIDE_WIDTH
            }

            DisplayHint.FIXED_EXTRA_WIDE -> {
                FIXED_EXTRA_WIDE_WIDTH
            }
        }
    }
}
