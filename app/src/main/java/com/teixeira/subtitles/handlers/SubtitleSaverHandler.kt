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
import com.teixeira.subtitles.utils.ToastUtils
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel

class SubtitleSaverHandler(
  val context: Context,
  val registry: ActivityResultRegistry,
  val subtitlesViewModel: SubtitlesViewModel,
) : DefaultLifecycleObserver {

  companion object {
    const val KEY_SUBTITLE_SAVER = "key_subtitle_saver"
  }

  private lateinit var subtitleSaver: ActivityResultLauncher<String>

  override fun onCreate(owner: LifecycleOwner) {
    subtitleSaver =
      registry.register(
        KEY_SUBTITLE_SAVER,
        owner,
        ActivityResultContracts.CreateDocument("text/*"),
        this::onSubtitleSave,
      )
  }

  override fun onDestroy(owner: LifecycleOwner) {
    subtitleSaver.unregister()
  }

  fun launchSaver() {
    val subtitle =
      subtitlesViewModel.subtitle
        ?: run {
          ToastUtils.showShort(R.string.error_no_subtitles_to_export)
          return
        }

    subtitleSaver.launch(subtitle.fullName)
  }

  private fun onSubtitleSave(uri: Uri?) {
    if (uri != null) {
      val subtitle = subtitlesViewModel.subtitle ?: return
      val subtitleFile = UriUtils.uri2File(uri)

      try {
        context.contentResolver.openOutputStream(uri)?.use {
          it.write(subtitle.toText().toByteArray())
        }
        ToastUtils.showLong(R.string.proj_export_saved, subtitleFile.getAbsolutePath())
      } catch (e: Exception) {
        ToastUtils.showLong(R.string.error_exporting_subtitles)
      }
    }
  }
}
