package com.example.android.architecture.blueprints.todoapp.domain

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test

class RunSummaryCalculatorTest {

    private val calculator = RunSummaryCalculator()

    @Test
    fun calculate_validPoints_returnsDistanceAndPace() {
        val points = listOf(
            GpxTrackPoint(25.0330, 121.5654, null, Instant.parse("2025-01-01T00:00:00Z")),
            GpxTrackPoint(25.0340, 121.5664, null, Instant.parse("2025-01-01T00:05:00Z")),
            GpxTrackPoint(25.0350, 121.5674, null, Instant.parse("2025-01-01T00:10:00Z")),
        )

        val result = calculator.calculate(points)

        assertThat(result.isSuccess).isTrue()
        val draft = result.getOrThrow()
        assertThat(draft.totalDistanceMeters).isGreaterThan(0.0)
        assertThat(draft.averagePaceSecPerKm).isNotNull()
        assertThat(draft.startTime).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"))
        assertThat(draft.endTime).isEqualTo(Instant.parse("2025-01-01T00:10:00Z"))
    }

    @Test
    fun calculate_missingTimestamp_returnsFailure() {
        val points = listOf(
            GpxTrackPoint(25.0330, 121.5654, null, Instant.parse("2025-01-01T00:00:00Z")),
            GpxTrackPoint(25.0340, 121.5664, null, null),
        )

        val result = calculator.calculate(points)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("缺少時間")
    }
}
