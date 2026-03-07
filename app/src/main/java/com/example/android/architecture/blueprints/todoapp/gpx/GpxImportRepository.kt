package com.example.android.architecture.blueprints.todoapp.gpx

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.example.android.architecture.blueprints.todoapp.domain.GpxTrackPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GpxImportRepository @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val gpxParser: GpxParser,
) {

    suspend fun importFromUri(uri: Uri): Result<GpxImportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val rawContent = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: throw GpxParseException("Unable to read selected file")

            val points = gpxParser.parse(rawContent).getOrThrow()
            GpxImportResult(
                fileName = resolveFileName(context.contentResolver, uri),
                points = points,
            )
        }
    }

    private fun resolveFileName(contentResolver: ContentResolver, uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return uri.lastPathSegment ?: "Unknown GPX"
    }
}

data class GpxImportResult(
    val fileName: String,
    val points: List<GpxTrackPoint>,
)
