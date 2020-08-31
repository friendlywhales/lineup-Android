package me.line_up.lineup.util

import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import me.line_up.lineup.App
import java.io.File
import java.io.FileOutputStream

class MediaImages {
    fun contentUriToFile(contentUri: Uri): File {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = App.shared.contentResolver.query(contentUri, proj, null, null, null)
            if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                val path = cursor.getString(columnIndex)
                Log.d(App.TAG, "Image file path: $path")
                if (!path.toLowerCase().endsWith(".jpeg") &&
                    !path.toLowerCase().endsWith(".jpg")) {
                    // 서버 골치 아프지 않게 싹 다 jpeg으로 인코딩
                    return transcodeToJpeg(contentUri)
                }
                return File(path)
            }
        } finally {
            cursor?.close()
        }
        throw Exception("No way")
    }

    private fun transcodeToJpeg(contentUri: Uri): File {
        val contentResolver = App.shared.contentResolver
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentUri)

        val file = File.createTempFile("lineup-image", "jpeg", App.shared.cacheDir)
        val stream = FileOutputStream(file, false)
        stream.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return file
    }
}