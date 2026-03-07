package com.example.android.architecture.blueprints.todoapp.domain

import com.example.android.architecture.blueprints.todoapp.gpx.GpxParseException
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RunSummaryCalculator @Inject constructor() {

    fun calculate(points: List<GpxTrackPoint>): Result<RunSessionDraft> = runCatching {
        if (points.size < 2) {
            throw GpxParseException("至少需要 2 個點位才能計算距離")
        }

        if (points.any { it.time == null }) {
            throw GpxParseException("點位缺少時間欄位，無法進入同步")
        }

        val startTime = points.first().time ?: error("checked above")
        val endTime = points.last().time ?: error("checked above")

        if (endTime.isBefore(startTime)) {
            throw GpxParseException("時間資料異常，結束時間早於開始時間")
        }

        val totalDistanceMeters = points.zipWithNext { current, next ->
            haversineMeters(
                lat1 = current.lat,
                lon1 = current.lng,
                lat2 = next.lat,
                lon2 = next.lng,
            )
        }.sum()

        val averagePaceSecPerKm = calculatePace(
            startTime = startTime,
            endTime = endTime,
            totalDistanceMeters = totalDistanceMeters,
        )

        RunSessionDraft(
            startTime = startTime,
            endTime = endTime,
            points = points,
            totalDistanceMeters = totalDistanceMeters,
            averagePaceSecPerKm = averagePaceSecPerKm,
        )
    }

    private fun calculatePace(
        startTime: Instant,
        endTime: Instant,
        totalDistanceMeters: Double,
    ): Double? {
        if (totalDistanceMeters <= 0.0) return null
        val totalSeconds = Duration.between(startTime, endTime).seconds
        if (totalSeconds <= 0L) return null
        val distanceKm = totalDistanceMeters / 1000.0
        return totalSeconds / distanceKm
    }

    private fun haversineMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val originLat = Math.toRadians(lat1)
        val destinationLat = Math.toRadians(lat2)

        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(originLat) * cos(destinationLat) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }
}
