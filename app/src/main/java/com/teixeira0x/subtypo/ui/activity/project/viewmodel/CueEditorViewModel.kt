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
import com.teixeira0x.subtypo.domain.model.Cue
import com.teixeira0x.subtypo.domain.usecase.subtitle.GetSubtitleUseCase
import com.teixeira0x.subtypo.domain.usecase.subtitle.UpdateSubtitleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CueEditorViewModel
@Inject
constructor(
  private val getSubtitleUseCase: GetSubtitleUseCase,
  private val updateSubtitleUseCase: UpdateSubtitleUseCase,
) : ViewModel() {

  private val _state = MutableLiveData<CueEditorState>(CueEditorState.Loading)

  val stateData: LiveData<CueEditorState>
    get() = _state

  fun loadCue(subtitleId: Long, cueIndex: Int) {
    viewModelScope.launch {
      val subtitle = getSubtitleUseCase(subtitleId).first()

      _state.postValue(
        CueEditorState.Loaded(subtitle!!.cues.getOrNull(cueIndex))
      )
    }
  }

  fun insertCue(
    subtitleId: Long,
    startTime: Long,
    endTime: Long,
    text: String,
  ) {
    viewModelScope.launch {
      val subtitle = getSubtitleUseCase(subtitleId).first()

      updateSubtitleUseCase(
        subtitle!!.copy(
          cues =
            subtitle.cues.toMutableList().apply {
              add(Cue(startTime = startTime, endTime = endTime, text = text))
            }
        )
      )
      _state.postValue(CueEditorState.Inserted)
    }
  }

  fun updateCue(
    subtitleId: Long,
    cueIndex: Int,
    startTime: Long,
    endTime: Long,
    text: String,
  ) {
    viewModelScope.launch {
      val subtitle = getSubtitleUseCase(subtitleId).first()

      updateSubtitleUseCase(
        subtitle!!.copy(
          cues =
            subtitle.cues.toMutableList().apply {
              set(
                cueIndex,
                Cue(startTime = startTime, endTime = endTime, text = text),
              )
            }
        )
      )
      _state.postValue(CueEditorState.Inserted)
    }
  }

  fun removeCue(subtitleId: Long, cueIndex: Int) {
    viewModelScope.launch {
      val subtitle = getSubtitleUseCase(subtitleId).first()

      updateSubtitleUseCase(
        subtitle!!.copy(
          cues = subtitle.cues.toMutableList().apply { removeAt(cueIndex) }
        )
      )
      _state.postValue(CueEditorState.Removed)
    }
  }

  sealed interface CueEditorState {
    object Loading : CueEditorState

    class Loaded(val cue: Cue?) : CueEditorState

    object Removed : CueEditorState

    object Inserted : CueEditorState

    class Error(val message: Int) : CueEditorState
  }
}
