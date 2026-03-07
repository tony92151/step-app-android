package com.example.android.architecture.blueprints.todoapp.data.local

data class ImportedRunEntity(
    val id: String,
    val fileName: String,
    val importedAtMillis: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val pointCount: Int,
    val totalDistanceMeters: Double,
    val status: String,
    val errorMessage: String?,
)
