package com.teixeira0x.subtypo.utils

import android.net.Uri
import java.io.File

object UriUtils {

  @JvmStatic
  val Uri.uri2File: File
    get() = com.blankj.utilcode.util.UriUtils.uri2File(this)
}
