package fi.vm.dpm.diff.model.metrics

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

data class TimeMetric(
    val name: String,
    val completedSteps: Int,
    val totalDuration: Duration,
    val cumulativeMovingAverageDuration: Duration,
    val minDuration: Duration,
    val maxDuration: Duration,
    val startAt: Instant?
) {
    companion object {

        fun empty(name: String): TimeMetric {
            return TimeMetric(
                name = name,
                completedSteps = 0,
                totalDuration = Duration.ZERO,
                cumulativeMovingAverageDuration = Duration.ZERO,
                minDuration = Duration.of(1, ChronoUnit.DAYS),
                maxDuration = Duration.ZERO,
                startAt = null
            )
        }
    }

    fun toEmpty(): TimeMetric {
        return TimeMetric.empty(name = name)
    }

    fun toStarted(): TimeMetric {
        check(startAt == null)

        return copy(
            startAt = Instant.now()
        )
    }

    fun toStopped(): TimeMetric {
        check(startAt != null)

        val endAt = Instant.now()
        val stepDuration = Duration.between(startAt, endAt)

        return copy(
            completedSteps = completedSteps + 1,
            totalDuration = totalDuration + stepDuration,
            cumulativeMovingAverageDuration =
            (cumulativeMovingAverageDuration.multipliedBy(completedSteps.toLong()) + stepDuration)
                .dividedBy(
                    (completedSteps + 1).toLong()
                ),
            minDuration = minOf(minDuration, stepDuration),
            maxDuration = maxOf(maxDuration, stepDuration),
            startAt = null
        )
    }
}
