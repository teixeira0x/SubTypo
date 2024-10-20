/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira.subtitles.handlers

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.teixeira.subtitles.R
import com.teixeira.subtitles.ext.cancelIfActive
import com.teixeira.subtitles.ext.launchWithProgress
import com.teixeira.subtitles.utils.ToastUtils
import com.teixeira.subtitles.utils.UriUtils.uri2File
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubtitleExporterHandler(
  val context: Context,
  val registry: ActivityResultRegistry,
  val subtitlesViewModel: SubtitlesViewModel,
) : DefaultLifecycleObserver {

  companion object {
    const val KEY_SUBTITLE_EXPORTER = "key_subtitle_exporter"
  }

  private lateinit var subtitleSaver: ActivityResultLauncher<String>

  private val scope = CoroutineScope(Dispatchers.Default)

  override fun onCreate(owner: LifecycleOwner) {
    subtitleSaver =
      registry.register(
        KEY_SUBTITLE_EXPORTER,
        owner,
        ActivityResultContracts.CreateDocument("text/*"),
        this::onCreateFileUri,
      )
  }

  override fun onDestroy(owner: LifecycleOwner) {
    subtitleSaver.unregister()
    scope.cancelIfActive("SubtitleExporterHandler has been destroyed")
  }

  fun launchSaver() {
    val subtitle =
      subtitlesViewModel.subtitle
        ?: run {
          ToastUtils.showShort(R.string.proj_export_subtitle_error_no_subtitles)
          return
        }

    subtitleSaver.launch(subtitle.fullName)
  }

  private fun onCreateFileUri(uri: Uri?) {
    if (uri == null) return

    scope.launchWithProgress(
      uiContext = context,
      configureBuilder = {
        setMessage(R.string.msg_please_wait)
      },
    ) {
      try {
        val subtitle = subtitlesViewModel.subtitle ?: return@launchWithProgress
    val subtitleFile = uri.uri2File
        val outputStream = context.contentResolver.openOutputStream(uri)
        if (outputStream != null) {
          outputStream.write(subtitle.toText().toByteArray())
          
          withContext(Dispatchers.Main) {
            ToastUtils.showLong(R.string.proj_export_subtitle_saved, subtitleFile.absolutePath)
          }
        }
      } catch (ioe: IOException) {
        withContext(Dispatchers.Main) {
          ToastUtils.showLong(R.string.proj_export_subtitle_error)
        }
      }
    }
  }
}
