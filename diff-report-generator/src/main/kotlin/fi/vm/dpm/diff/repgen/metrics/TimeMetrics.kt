package fi.vm.dpm.diff.model.metrics

import de.m3y.kformat.Table
import de.m3y.kformat.table
import fi.vm.dpm.diff.model.thisShouldNeverHappen
import java.time.Duration

class TimeMetrics<K> {

    private val metricsByKey: MutableMap<K, TimeMetric> = mutableMapOf()

    fun createMetric(key: K, name: String) {
        check(!metricsByKey.contains(key))
        metricsByKey[key] = TimeMetric.empty(name)
    }

    fun reset() {
        metricsByKey.forEach { (key, metric) ->
            metricsByKey[key] = metric.toEmpty()
        }
    }

    fun report(): String {
        val sb = StringBuilder()

        table {
            header(
                "",
                "Steps",
                "Min (sec/step)",
                "Max (sec/step)",
                "Average (sec/step)",
                "Total (sec)"
            )

            metricsByKey.values.forEach { metric ->
                row(
                    metric.name,
                    metric.completedSteps,
                    metric.minDuration.toSecondsAndMillis(),
                    metric.maxDuration.toSecondsAndMillis(),
                    metric.cumulativeMovingAverageDuration.toSecondsAndMillis(),
                    metric.totalDuration.toSecondsAndMillis()
                )
            }

            hints {
                borderStyle = Table.BorderStyle.SINGLE_LINE
            }
        }.render(sb)

        return sb.toString()
    }

    fun startStep(key: K) {
        val metric = metricsByKey[key] ?: thisShouldNeverHappen("No metric for key: $key")
        metricsByKey[key] = metric.toStarted()
    }

    fun stopStep(key: K) {
        val metric = metricsByKey[key] ?: thisShouldNeverHappen("No metric for key: $key")
        metricsByKey[key] = metric.toStopped()
    }

    private fun Duration.toSecondsAndMillis(): String {
        val fullMillis = toMillis()
        val seconds = fullMillis / 1_000
        val millis = fullMillis % 1_000

        return "$seconds.$millis"
    }
}
