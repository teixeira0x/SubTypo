package com.teixeira.subtitles.utils

import android.net.Uri
import java.io.File

object UriUtils {

  @JvmStatic
  val Uri.uri2File: File
    get() = com.blankj.utilcode.util.UriUtils.uri2File(this)
}
