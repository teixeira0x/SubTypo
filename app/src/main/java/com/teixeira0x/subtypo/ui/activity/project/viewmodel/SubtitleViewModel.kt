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

package com.teixeira0x.subtypo.ui.activity.project.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.UriUtils
import org.slf4j.LoggerFactory;
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.domain.model.Subtitle
import com.teixeira0x.subtypo.domain.subtitle.mapper.SubtitleFormatMapper.toSubtitleFormat
import com.teixeira0x.subtypo.domain.usecase.subtitle.GetAllSubtitlesUseCase
import com.teixeira0x.subtypo.domain.usecase.subtitle.InsertSubtitleUseCase
import com.teixeira0x.subtypo.domain.usecase.subtitle.RemoveSubtitleUseCase
import com.teixeira0x.subtypo.ui.viewmodel.event.ViewEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SubtitleViewModel
@Inject
constructor(
  private val getAllSubtitlesUseCase: GetAllSubtitlesUseCase,
  private val insertSubtitleUseCase: InsertSubtitleUseCase,
  private val removeSubtitleUseCase: RemoveSubtitleUseCase,
) : ViewModel() {

  companion object {
    private val log = LoggerFactory.getLogger(SubtitleViewModel::class.java)
  }

  private val _viewEvent = MutableLiveData<ViewEvent>()
  val viewEventData: LiveData<ViewEvent>
    get() = _viewEvent

  private val _state = MutableLiveData<SubtitleState>(SubtitleState.Loading)
  val stateData: LiveData<SubtitleState>
    get() = _state

  private val _selectedSubtitleId = MutableLiveData<Long>(-1)
  val selectedSubtitleId: Long
    get() = _selectedSubtitleId.value!!

  private val subtitles = mutableListOf<Subtitle>()

  fun loadSubtitles(projectId: Long) {
    viewModelScope.launch {
      getAllSubtitlesUseCase(projectId).collect { subtitleList ->
        subtitles.clear()
        subtitles.addAll(subtitleList)

        val selectedSubtitle =
          subtitles.find { it.id == selectedSubtitleId }
            ?: subtitles.firstOrNull()

        _selectedSubtitleId.value = selectedSubtitle?.id ?: -1
        _state.value = SubtitleState.Loaded(subtitleList, selectedSubtitle)
      }
    }
  }

  fun selectSubtitle(newSelectedSubtitleId: Long) {
    viewModelScope.launch {
      val newSelectedSubtitle =
        subtitles.find { it.id == newSelectedSubtitleId }

      _selectedSubtitleId.value = newSelectedSubtitle?.id ?: -1
      _state.value =
        SubtitleState.Loaded(subtitles.toList(), newSelectedSubtitle)
    }
  }

  fun getSelectedSubtitleFullname(): String {
    return subtitles
      .find { it.id == selectedSubtitleId }
      ?.let { it.name + it.format.extension } ?: ""
  }

  fun writeSelectedSubtitle(uri: Uri, contentResolver: ContentResolver) {
    viewModelScope.launch {
      val subtitle =
        subtitles.find { it.id == selectedSubtitleId } ?: return@launch
      contentResolver.openOutputStream(uri)?.use { outputStream ->
        ViewEvent.Toast(
            try {
              outputStream.write(
                subtitle.format.toText(subtitle.cues).toByteArray()
              )

              R.string.proj_export_subtitle_saved
            } catch (e: Exception) {
              log.error("Export subtitle error", e)
              R.string.proj_export_subtitle_error
            }
          )
          .also { _viewEvent.value = it }
      }
    }
  }

  fun importSubtitleFile(
    projectId: Long,
    uri: Uri,
    contentResolver: ContentResolver,
  ) {
    viewModelScope.launch {
      val resultMessage =
        try {
          val file = UriUtils.uri2File(uri)
          val fileContent =
            contentResolver.openInputStream(uri)?.bufferedReader()?.use {
              it.readText()
            } ?: ""

          val format = ".srt".toSubtitleFormat()
          val cues = format.parseText(fileContent)

          val id =
            insertSubtitleUseCase(
              Subtitle(
                projectId = projectId,
                name = file.name,
                format = format,
                cues = cues,
              )
            )

          if (id > 0) {
            selectSubtitle(id)

            R.string.proj_import_subtitle_success
          } else {
            R.string.proj_import_subtitle_error
          }
        } catch (e: Exception) {
          log.error("Import subtitle error", e)
          R.string.proj_import_subtitle_error
        }

      _viewEvent.value = ViewEvent.Toast(resultMessage)
    }
  }

  sealed interface SubtitleState {
    data object Loading : SubtitleState

    data class Loaded(
      val subtitles: List<Subtitle>,
      val selectedSubtitle: Subtitle?,
    ) : SubtitleState

    data object Removed : SubtitleState

    data class Error(val message: Int) : SubtitleState
  }
}
