package com.example.android.architecture.blueprints.todoapp.gpx

import com.example.android.architecture.blueprints.todoapp.domain.GpxTrackPoint
import java.io.StringReader
import java.time.Instant
import java.time.format.DateTimeParseException
import javax.inject.Inject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory

class GpxParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

class XmlGpxParser @Inject constructor() : GpxParser {

    override fun parse(rawGpx: String): Result<List<GpxTrackPoint>> {
        if (rawGpx.isBlank()) {
            return Result.failure(GpxParseException("GPX file is empty"))
        }

        return runCatching {
            val parser = XmlPullParserFactory.newInstance().newPullParser().apply {
                setInput(StringReader(rawGpx))
            }

            val points = mutableListOf<GpxTrackPoint>()
            var currentLat: Double? = null
            var currentLng: Double? = null
            var currentEle: Double? = null
            var currentTime: Instant? = null
            var insideTrackPoint = false

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "trkpt" -> {
                                insideTrackPoint = true
                                currentLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                                currentLng = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
                                currentEle = null
                                currentTime = null
                            }

                            "ele" -> {
                                if (insideTrackPoint) {
                                    currentEle = parser.nextText().toDoubleOrNull()
                                }
                            }

                            "time" -> {
                                if (insideTrackPoint) {
                                    currentTime = parseInstant(parser.nextText())
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "trkpt") {
                            if (currentLat != null && currentLng != null) {
                                points += GpxTrackPoint(
                                    lat = currentLat,
                                    lng = currentLng,
                                    elevation = currentEle,
                                    time = currentTime,
                                )
                            }
                            insideTrackPoint = false
                        }
                    }
                }
                parser.next()
            }

            if (points.isEmpty()) {
                throw GpxParseException("No valid coordinate points found in GPX")
            }

            if (points.none { it.time != null }) {
                throw GpxParseException("GPX is missing timestamp data")
            }

            points
        }.recoverCatching { error ->
            throw when (error) {
                is GpxParseException -> error
                is XmlPullParserException -> GpxParseException("Invalid GPX XML format", error)
                else -> GpxParseException("Failed to parse GPX: ${error.message ?: "Unknown error"}", error)
            }
        }
    }

    private fun parseInstant(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return try {
            Instant.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
