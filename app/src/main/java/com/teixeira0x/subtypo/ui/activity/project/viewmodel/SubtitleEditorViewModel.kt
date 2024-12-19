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
import com.teixeira0x.subtypo.domain.usecase.subtitle.GetSubtitleUseCase
import com.teixeira0x.subtypo.domain.usecase.subtitle.InsertSubtitleUseCase
import com.teixeira0x.subtypo.domain.usecase.subtitle.RemoveSubtitleUseCase
import com.teixeira0x.subtypo.domain.usecase.subtitle.UpdateSubtitleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel
class SubtitleEditorViewModel
@Inject
constructor(
  private val getSubtitleUseCase: GetSubtitleUseCase,
  private val insertSubtitleUseCase: InsertSubtitleUseCase,
  private val updateSubtitleUseCase: UpdateSubtitleUseCase,
  private val removeSubtitleUseCase: RemoveSubtitleUseCase,
) : ViewModel() {

  private val _state =
    MutableLiveData<SubtitleEditorState>(SubtitleEditorState.Loading)

  val stateData: LiveData<SubtitleEditorState>
    get() = _state

  fun loadSubtitle(subtitleId: Long) {
    viewModelScope.launch {
      val subtitle = getSubtitleUseCase(subtitleId).firstOrNull()
      _state.postValue(SubtitleEditorState.Loaded(subtitle))
    }
  }

  fun insertSubtitle(projectId: Long, name: String) {
    viewModelScope.launch {
      val id =
        insertSubtitleUseCase(
          Subtitle(projectId = projectId, name = name, cues = emptyList())
        )

      _state.postValue(
        if (id > 0) {
          SubtitleEditorState.Inserted(id)
        } else SubtitleEditorState.Error(R.string.subtitle_error_remove)
      )
    }
  }

  fun updateSubtitle(subtitleId: Long, name: String) {
    viewModelScope.launch {
      val subtitle = getSubtitleUseCase(subtitleId).first()

      updateSubtitleUseCase(subtitle!!.copy(name = name))

      _state.postValue(SubtitleEditorState.Updated)
    }
  }

  fun removeSubtitle(subtitleId: Long) {
    viewModelScope.launch {
      val deletedRows = removeSubtitleUseCase(subtitleId)

      _state.postValue(
        if (deletedRows > 0) {
          SubtitleEditorState.Removed
        } else SubtitleEditorState.Error(R.string.subtitle_error_remove)
      )
    }
  }

  sealed interface SubtitleEditorState {
    object Loading : SubtitleEditorState

    data class Loaded(val subtitle: Subtitle?) : SubtitleEditorState

    data class Inserted(val id: Long) : SubtitleEditorState

    object Updated : SubtitleEditorState

    object Removed : SubtitleEditorState

    class Error(val message: Int) : SubtitleEditorState
  }
}
