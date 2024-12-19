package com.teixeira0x.subtypo.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.IOException

object VideoUtils {
  fun Context.getVideoThumbnail(path: String): Bitmap? {
    return getVideoThumbnail(Uri.fromFile(File(path)))
  }

  fun Context.getVideoThumbnail(uri: Uri): Bitmap? {
    var thumbnail: Bitmap? = null
    val retriever = MediaMetadataRetriever()

    try {
      retriever.setDataSource(this, uri)
      thumbnail = retriever.getFrameAtTime()
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      try {
        retriever.release()
      } catch (ioe: IOException) {
        // Nothing
      }
    }

    return thumbnail
  }
}
