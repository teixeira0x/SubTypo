package com.teixeira0x.subtypo.ui.activity.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.domain.model.Subtitle
import com.teixeira0x.subtypo.domain.usecase.project.GetProjectUseCase
import com.teixeira0x.subtypo.domain.usecase.project.InsertProjectUseCase
import com.teixeira0x.subtypo.domain.usecase.project.UpdateProjectUseCase
import com.teixeira0x.subtypo.domain.usecase.subtitle.InsertSubtitleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

  private val _state =
    MutableLiveData<ProjectEditorState>(ProjectEditorState.Loading)

  val stateData: LiveData<ProjectEditorState>
    get() = _state

  fun loadProject(id: Long) {
    viewModelScope.launch {
      getProjectUseCase(id).collect { project ->
        _state.postValue(ProjectEditorState.Loaded(project))
      }
    }
  }

  fun createProject(name: String, videoUri: String) {
    viewModelScope.launch {
      val projectId =
        insertProjectUseCase(Project(id = 0, name = name, videoUri = videoUri))

      insertSubtitleUseCase(
        Subtitle(projectId = projectId, name = "subtitle", cues = emptyList())
      )

      _state.value = ProjectEditorState.Created(projectId, true)
    }
  }

  fun updateProject(id: Long, name: String, videoUri: String) {
    viewModelScope.launch {
      updateProjectUseCase(Project(name = name, videoUri = videoUri))
      _state.value = ProjectEditorState.Created(id, false)
    }
  }

  fun onCreating() {
    _state.postValue(ProjectEditorState.Creating)
  }

  sealed interface ProjectEditorState {
    object Loading : ProjectEditorState

    data class Loaded(val project: Project?) : ProjectEditorState

    object Creating : ProjectEditorState

    data class Created(val projectId: Long, val openProject: Boolean) :
      ProjectEditorState
  }
}
