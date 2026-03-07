package com.example.android.architecture.blueprints.todoapp.gpx

import com.example.android.architecture.blueprints.todoapp.domain.GpxTrackPoint

interface GpxParser {
    fun parse(rawGpx: String): Result<List<GpxTrackPoint>>
}
