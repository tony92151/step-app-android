package com.example.android.architecture.blueprints.todoapp.domain

import java.time.Instant

data class GpxTrackPoint(
    val lat: Double,
    val lng: Double,
    val elevation: Double?,
    val time: Instant?,
)

data class RunSessionDraft(
    val startTime: Instant,
    val endTime: Instant,
    val points: List<GpxTrackPoint>,
    val totalDistanceMeters: Double,
    val averagePaceSecPerKm: Double?,
)
