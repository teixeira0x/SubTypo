package com.teixeira0x.subtypo.ui.activity.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.domain.usecase.project.GetProjectUseCase
import com.teixeira0x.subtypo.domain.usecase.project.InsertProjectUseCase
import com.teixeira0x.subtypo.domain.usecase.project.RemoveProjectUseCase
import com.teixeira0x.subtypo.domain.usecase.project.UpdateProjectUseCase
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
  private val removeProjectUseCase: RemoveProjectUseCase,
) : ViewModel() {

  private val _state =
    MutableLiveData<ProjectEditorState>(ProjectEditorState.Loading)

  val stateData: LiveData<ProjectEditorState>
    get() = _state

  fun getProject(id: Long, onCollect: (Project) -> Unit) {
    viewModelScope.launch {
      getProjectUseCase(id).collect { project ->
        if (project != null) {
          _state.postValue(ProjectEditorState.Loaded)
          onCollect(project)
        }
      }
    }
  }

  fun createProject(name: String, videoUri: String, onInsert: (Long) -> Unit) {
    viewModelScope.launch {
      val id =
        insertProjectUseCase(Project(id = 0, name = name, videoUri = videoUri))

      onInsert(id)
    }
  }

  fun updateProject(
    id: Long,
    name: String,
    videoUri: String,
    onUpdate: () -> Unit,
  ) {
    viewModelScope.launch {
      updateProjectUseCase(Project(id = id, name = name, videoUri = videoUri))

      onUpdate()
    }
  }

  sealed interface ProjectEditorState {
    object Loading : ProjectEditorState

    object Loaded : ProjectEditorState
  }
}
