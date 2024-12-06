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

package com.teixeira0x.subtypo.handlers

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.ext.cancelIfActive
import com.teixeira0x.subtypo.ext.launchWithProgress
import com.teixeira0x.subtypo.subtitle.formats.SubtitleFormat
import com.teixeira0x.subtypo.subtitle.models.Subtitle
import com.teixeira0x.subtypo.utils.ToastUtils
import com.teixeira0x.subtypo.utils.UriUtils.uri2File
import com.teixeira0x.subtypo.viewmodels.SubtitlesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubtitlePickerHandler(
  val context: Context,
  val registry: ActivityResultRegistry,
  val subtitlesViewModel: SubtitlesViewModel,
) : DefaultLifecycleObserver {

  companion object {
    const val KEY_PICK_SUBTITLE = "key_pick_subtitle"
  }

  private lateinit var subtitlePicker: ActivityResultLauncher<String>

  private val scope = CoroutineScope(Dispatchers.Default)

  override fun onCreate(owner: LifecycleOwner) {
    subtitlePicker =
      registry.register(
        KEY_PICK_SUBTITLE,
        owner,
        ActivityResultContracts.GetContent(),
        this::onPickSubtitle,
      )
  }

  override fun onDestroy(owner: LifecycleOwner) {
    subtitlePicker.unregister()
    scope.cancelIfActive("SubtitlePickerHandler has been destroyed")
  }

  fun launchPicker() {
    subtitlePicker.launch("*/*")
  }

  private fun onPickSubtitle(uri: Uri?) {
    if (uri == null) return

    scope.launchWithProgress(
      uiContext = context,
      configureBuilder = {
        setMessage(R.string.msg_please_wait)
        setCancelable(false)
      },
    ) {
      try {
        val contentResolver = context.contentResolver
        val text =
          contentResolver.openInputStream(uri)?.bufferedReader()?.readText()

        withContext(Dispatchers.Main) {
          if (text == null || text.trim().isEmpty()) {
            ToastUtils.showLong(R.string.proj_import_subtitle_error)
            return@withContext
          }

          val file = uri.uri2File
          val subtitleFormat =
            SubtitleFormat.Builder.from(".${file.extension}").build()
          val paragraphs = subtitleFormat.parseText(text)

          if (subtitleFormat.errorList.isNotEmpty()) {
            ToastUtils.showLong(R.string.proj_import_subtitle_error)
            return@withContext
          }

          subtitlesViewModel.addSubtitle(
            subtitle =
              Subtitle(
                name = file.name.substringBeforeLast("."),
                subtitleFormat = subtitleFormat,
                paragraphs = paragraphs,
              ),
            select = true,
          )
          ToastUtils.showLong(R.string.proj_import_subtitle_success)
        }
      } catch (ioe: Exception) {
        withContext(Dispatchers.Main) {
          ToastUtils.showLong(R.string.proj_import_subtitle_error)
        }
      }
    }
  }
}
