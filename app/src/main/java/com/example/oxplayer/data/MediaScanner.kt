package com.example.oxplayer.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val isVideo: Boolean
)

/**
 * Reads the device's MediaStore index instead of walking the filesystem by hand.
 * This is what makes scanning fast and keeps it working across Android's scoped
 * storage restrictions without needing broad file permissions.
 */
object MediaScanner {

    fun scanAll(context: Context): List<MediaItem> {
        return scanVideos(context) + scanAudio(context)
    }

    fun scanVideos(context: Context): List<MediaItem> =
        query(
            context = context,
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            isVideo = true
        )

    fun scanAudio(context: Context): List<MediaItem> =
        query(
            context = context,
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            isVideo = false
        )

    private fun query(context: Context, collection: Uri, isVideo: Boolean): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.SIZE
        )
        val items = mutableListOf<MediaItem>()

        context.contentResolver.query(
            collection, projection, null, null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                items += MediaItem(
                    id = id,
                    uri = uri,
                    title = cursor.getString(nameCol) ?: "Unknown",
                    durationMs = cursor.getLong(durCol),
                    sizeBytes = cursor.getLong(sizeCol),
                    isVideo = isVideo
                )
            }
        }
        return items
    }
}
