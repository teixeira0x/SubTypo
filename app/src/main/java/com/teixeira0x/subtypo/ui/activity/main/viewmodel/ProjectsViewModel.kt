package com.teixeira0x.subtypo.ui.activity.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.domain.usecase.project.GetAllProjectsUseCase
import com.teixeira0x.subtypo.domain.usecase.project.RemoveProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

// import kotlinx.coroutines.flow.collect

@HiltViewModel
class ProjectsViewModel
@Inject
constructor(
  private val getAllProjectsUseCase: GetAllProjectsUseCase,
  private val removeProjectUseCase: RemoveProjectUseCase,
) : ViewModel() {

  private val _state = MutableLiveData<ProjectsState>(ProjectsState.Loading)

  val stateData: LiveData<ProjectsState>
    get() = _state

  init {
    loadProjects()
  }

  private fun loadProjects() {
    viewModelScope.launch {
      getAllProjectsUseCase().collect { projects ->
        _state.postValue(ProjectsState.Loaded(projects))
      }
    }
  }

  fun deleteProject(projectId: Long, onDelete: (Boolean) -> Unit) {
    viewModelScope.launch {
      val rowsDeleted = removeProjectUseCase(projectId)
      onDelete(rowsDeleted > 0)
    }
  }

  sealed interface ProjectsState {
    object Loading : ProjectsState

    data class Loaded(val projects: List<Project>) : ProjectsState
  }
}
