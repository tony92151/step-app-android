package com.example.android.architecture.blueprints.todoapp.gpx

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class XmlGpxParserTest {

    private val parser = XmlGpxParser()

    @Test
    fun parse_validGpx_returnsTrackPoints() {
        val rawGpx = """
            <gpx version="1.1" creator="test">
              <trk>
                <trkseg>
                  <trkpt lat="25.0330" lon="121.5654">
                    <ele>10.0</ele>
                    <time>2025-01-01T00:00:00Z</time>
                  </trkpt>
                  <trkpt lat="25.0340" lon="121.5664">
                    <time>2025-01-01T00:05:00Z</time>
                  </trkpt>
                </trkseg>
              </trk>
            </gpx>
        """.trimIndent()

        val result = parser.parse(rawGpx)

        assertThat(result.isSuccess).isTrue()
        val points = result.getOrThrow()
        assertThat(points).hasSize(2)
        assertThat(points[0].elevation).isEqualTo(10.0)
        assertThat(points[1].time).isNotNull()
    }

    @Test
    fun parse_invalidXml_returnsFailure() {
        val result = parser.parse("<gpx><trk>")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(GpxParseException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Invalid GPX XML format")
    }

    @Test
    fun parse_missingTime_returnsFailure() {
        val rawGpx = """
            <gpx>
              <trk>
                <trkseg>
                  <trkpt lat="25.0330" lon="121.5654" />
                </trkseg>
              </trk>
            </gpx>
        """.trimIndent()

        val result = parser.parse(rawGpx)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("missing timestamp")
    }
}
