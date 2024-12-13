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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.domain.model.Subtitle
import com.teixeira0x.subtypo.domain.usecase.subtitle.GetAllSubtitlesUseCase
import com.teixeira0x.subtypo.domain.usecase.subtitle.RemoveSubtitleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SubtitleViewModel
@Inject
constructor(
  private val getAllSubtitlesUseCase: GetAllSubtitlesUseCase,
  private val removeSubtitleUseCase: RemoveSubtitleUseCase,
) : ViewModel() {

  private val _state = MutableLiveData<SubtitleState>(SubtitleState.Loading)

  val stateData: LiveData<SubtitleState>
    get() = _state

  private val _selectedSubtitleId = MutableLiveData<Long>(0)
  val selectedSubtitleId: Long
    get() = _selectedSubtitleId.value!!

  fun loadSubtitles(projectId: Long) {
    viewModelScope.launch {
      getAllSubtitlesUseCase(projectId).collect { subtitles ->
        val selectedSubtitle = subtitles.firstOrNull()
        _state.postValue(SubtitleState.Loaded(subtitles, selectedSubtitle))
        _selectedSubtitleId.value = selectedSubtitle?.id ?: 0
      }
    }
  }

  fun removeSubtitle(subtitleId: Long) {
    viewModelScope.launch {
      val deletedRows = removeSubtitleUseCase(subtitleId)

      _state.postValue(
        if (deletedRows > 0) {
          SubtitleState.Removed
        } else SubtitleState.Error(R.string.subtitle_error_remove)
      )
    }
  }

  sealed interface SubtitleState {
    object Loading : SubtitleState

    class Loaded(
      val subtitles: List<Subtitle>,
      val selectedSubtitle: Subtitle?,
    ) : SubtitleState

    object Removed : SubtitleState

    class Error(val message: Int) : SubtitleState
  }
}
