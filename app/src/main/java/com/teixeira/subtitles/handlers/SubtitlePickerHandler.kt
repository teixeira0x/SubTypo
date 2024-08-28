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
import com.blankj.utilcode.util.UriUtils
import com.teixeira.subtitles.R
import com.teixeira.subtitles.subtitle.formats.SubtitleFormat
import com.teixeira.subtitles.subtitle.models.Subtitle
import com.teixeira.subtitles.utils.ToastUtils
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel

class SubtitlePickerHandler(
  val context: Context,
  val registry: ActivityResultRegistry,
  val subtitlesViewModel: SubtitlesViewModel,
) : DefaultLifecycleObserver {

  companion object {
    const val KEY_PICK_SUBTITLE = "key_pick_subtitle"
  }

  private lateinit var subtitlePicker: ActivityResultLauncher<String>

  override fun onCreate(owner: LifecycleOwner) {
    subtitlePicker =
      registry.register(
        KEY_PICK_SUBTITLE,
        owner,
        ActivityResultContracts.GetContent(),
        this::onPickSubtitle,
      )
  }

  fun launchPicker() {
    subtitlePicker.launch("*/*")
  }

  private fun onPickSubtitle(uri: Uri?) {
    if (uri == null) {
      return
    }

    try {
      val text =
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
          ?: return

      val file = UriUtils.uri2File(uri)
      val subtitleFormat = SubtitleFormat.getExtensionFormat(file.extension)
      val paragraphs = subtitleFormat.parseText(text)
      if (subtitleFormat.errorList.isNotEmpty()) {
        ToastUtils.showLong(R.string.proj_import_subtitle_error)
        return
      }

      subtitlesViewModel.addSubtitle(
        Subtitle(file.name.substringBeforeLast("."), subtitleFormat, paragraphs),
        true,
      )
      ToastUtils.showLong(R.string.proj_import_subtitle_success)
    } catch (e: Exception) {
      ToastUtils.showLong(R.string.proj_import_subtitle_error)
    }
  }
}
