package com.teixeira.subtypo.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.teixeira.subtypo.models.Project
import com.teixeira.subtypo.project.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectsViewModel : ViewModel() {

  private val _state = MutableLiveData<ProjectsState>(ProjectsState.Loading)

  val stateData: LiveData<ProjectsState> = _state

  fun loadProjects() {
    viewModelScope.launch {
      val projects = ProjectRepository.fetchProjects()
      _state.postValue(ProjectsState.Loaded(projects))
    }
  }

  fun createProject(name: String, videoUri: Uri, onProjectCreated: (project: Project) -> Unit) {
    _state.postValue(ProjectsState.Loading)
    viewModelScope.launch {
      val project = ProjectRepository.createProject(name, videoUri)

      withContext(Dispatchers.Main) {
        onProjectCreated(project)
        loadProjects()
      }
    }
  }

  fun updateProject(
    id: String,
    name: String,
    videoUri: Uri,
    onProjectUpdated: (project: Project) -> Unit,
  ) {
    _state.postValue(ProjectsState.Loading)
    viewModelScope.launch {
      val project = ProjectRepository.updateProject(id, name, videoUri)
      if (project == null) {
        return@launch
      }

      withContext(Dispatchers.Main) {
        onProjectUpdated(project)
        loadProjects()
      }
    }
  }

  fun deleteProject(project: Project, onProjectDeleted: (deleted: Boolean) -> Unit) {
    _state.postValue(ProjectsState.Loading)
    viewModelScope.launch {
      val deleted = FileUtils.delete(project.path)

      withContext(Dispatchers.Main) {
        onProjectDeleted(deleted)
        loadProjects()
      }
    }
  }

  sealed class ProjectsState {
    data object Loading : ProjectsState()

    data class Loaded(val projects: List<Project>) : ProjectsState()
  }
}
