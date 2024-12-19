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

package com.teixeira0x.subtypo.ui.projecteditor.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.UriUtils
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.domain.model.Subtitle
import com.teixeira0x.subtypo.domain.usecase.project.GetProjectUseCase
import com.teixeira0x.subtypo.domain.usecase.project.InsertProjectUseCase
import com.teixeira0x.subtypo.domain.usecase.project.UpdateProjectUseCase
import com.teixeira0x.subtypo.domain.usecase.subtitle.InsertSubtitleUseCase
import com.teixeira0x.subtypo.ui.projecteditor.mvi.ProjectEditorIntent
import com.teixeira0x.subtypo.ui.projecteditor.mvi.ProjectEditorViewEvent
import com.teixeira0x.subtypo.ui.projecteditor.mvi.ProjectEditorViewState
import com.teixeira0x.subtypo.ui.viewmodel.event.ViewEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ProjectEditorViewModel
@Inject
constructor(
  private val getProjectUseCase: GetProjectUseCase,
  private val insertProjectUseCase: InsertProjectUseCase,
  private val updateProjectUseCase: UpdateProjectUseCase,
  private val insertSubtitleUseCase: InsertSubtitleUseCase,
) : ViewModel() {

  private val _projectEditorState =
    MutableStateFlow<ProjectEditorViewState>(ProjectEditorViewState.Loading)
  val projectEditorState: StateFlow<ProjectEditorViewState> =
    _projectEditorState.asStateFlow()

  private val _viewEvent = Channel<ViewEvent>(Channel.BUFFERED)
  val viewEvent: Flow<ViewEvent> = _viewEvent.receiveAsFlow()

  private val _customViewEvent = MutableSharedFlow<ProjectEditorViewEvent>()
  val customViewEvent: SharedFlow<ProjectEditorViewEvent> =
    _customViewEvent.asSharedFlow()

  var videoUri: Uri? = null

  fun doIntent(event: ProjectEditorIntent) {
    when (event) {
      is ProjectEditorIntent.Load -> loadProject(event)
      is ProjectEditorIntent.UpdateVideoUri -> updateVideoUri(event)
      is ProjectEditorIntent.Create -> insertProject(event)
      is ProjectEditorIntent.Update -> updateProject(event)
    }
  }

  private fun loadProject(event: ProjectEditorIntent.Load) {
    _projectEditorState.value = ProjectEditorViewState.Loading
    viewModelScope.launch {
      val project = getProjectUseCase(event.id).firstOrNull()

      _projectEditorState.value = ProjectEditorViewState.Loaded(project)
    }
  }

  private fun updateVideoUri(event: ProjectEditorIntent.UpdateVideoUri) {
    viewModelScope.launch {
      videoUri = event.uri
      _customViewEvent.emit(
        ProjectEditorViewEvent.UpdateVideo(UriUtils.uri2File(event.uri))
      )
    }
  }

  private fun insertProject(event: ProjectEditorIntent.Create) {
    viewModelScope.launch {
      val name = event.name
      val videoUri = event.videoUri

      if (name.isEmpty() || name.isBlank()) {
        _viewEvent.send(
          ViewEvent.Toast(R.string.proj_editor_error_name_required)
        )
        return@launch
      }

      val projectId =
        insertProjectUseCase(Project(id = 0, name = name, videoUri = videoUri))

      if (projectId > 0) {
        insertSubtitleUseCase(
          Subtitle(projectId = projectId, name = "subtitle", cues = emptyList())
        )

        _customViewEvent.emit(
          ProjectEditorViewEvent.NavigateToProject(projectId)
        )
      }

      _viewEvent.send(
        ViewEvent.Toast(
          if (projectId > 0) {
            R.string.proj_editor_success_create
          } else {
            R.string.common_failed
          }
        )
      )
    }
  }

  private fun updateProject(event: ProjectEditorIntent.Update) {
    viewModelScope.launch {
      val id = event.id
      val name = event.name
      val videoUri = event.videoUri

      if (name.isEmpty() || name.isBlank()) {
        _viewEvent.send(
          ViewEvent.Toast(R.string.proj_editor_error_name_required)
        )
        return@launch
      }

      val updated =
        updateProjectUseCase(Project(id = id, name = name, videoUri = videoUri))
      if (updated > 0) {
        _customViewEvent.emit(ProjectEditorViewEvent.Dismiss)
      }

      _viewEvent.send(
        ViewEvent.Toast(
          if (updated > 0) {
            R.string.proj_editor_success_update
          } else {
            R.string.common_failed
          }
        )
      )
    }
  }
}
