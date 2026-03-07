package com.example.android.architecture.blueprints.todoapp.gpx

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.example.android.architecture.blueprints.todoapp.domain.GpxTrackPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@Singleton
class GpxImportRepository @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val gpxParser: GpxParser,
) {

    private val _latestImport = MutableStateFlow<GpxImportResult?>(null)
    val latestImport: StateFlow<GpxImportResult?> = _latestImport.asStateFlow()

    private val _latestSyncState = MutableStateFlow<ImportedRunSyncState?>(null)
    val latestSyncState: StateFlow<ImportedRunSyncState?> = _latestSyncState.asStateFlow()

    suspend fun importFromUri(uri: Uri): Result<GpxImportResult> = withContext(Dispatchers.IO) {
        runCatching {
            // 嘗試取得持久讀取權限，讓 ContentResolver 可在 IO 執行緒正常開啟 URI。
            // 某些 URI（如 file://）不支援持久化授權，忽略 SecurityException。
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Non-persistable URI (e.g. file://), safe to ignore.
            }

            val rawContent = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: throw GpxParseException("Unable to read selected file")

            val points = gpxParser.parse(rawContent).getOrThrow()
            GpxImportResult(
                fileName = resolveFileName(context.contentResolver, uri),
                points = points,
            ).also {
                _latestImport.value = it
                _latestSyncState.value = ImportedRunSyncState(
                    fileName = it.fileName,
                    status = SyncStatus.NOT_SYNCED,
                    errorMessage = null,
                )
            }
        }
    }

    fun markSyncSuccess() {
        val latest = _latestImport.value ?: return
        _latestSyncState.value = ImportedRunSyncState(
            fileName = latest.fileName,
            status = SyncStatus.SUCCESS,
            errorMessage = null,
        )
    }

    fun markSyncFailure(errorMessage: String) {
        val latest = _latestImport.value ?: return
        _latestSyncState.value = ImportedRunSyncState(
            fileName = latest.fileName,
            status = SyncStatus.FAILED,
            errorMessage = errorMessage,
        )
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

data class ImportedRunSyncState(
    val fileName: String,
    val status: SyncStatus,
    val errorMessage: String?,
)

enum class SyncStatus {
    NOT_SYNCED,
    SUCCESS,
    FAILED,
}
