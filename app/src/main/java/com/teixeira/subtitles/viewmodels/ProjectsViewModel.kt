package com.teixeira.subtitles.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira.subtitles.models.Project
import com.teixeira.subtitles.project.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectsViewModel : ViewModel() {

  private val _projects = MutableLiveData<List<Project>>(listOf<Project>())

  val projects: List<Project>
    get() = _projects.value!!

  fun provideProjects() {
    viewModelScope.launch {
      val projects = ProjectRepository.fetchProjects()

      withContext(Dispatchers.Main) { _projects.value = projects }
    }
  }

  fun observeProjects(owner: LifecycleOwner, observer: Observer<List<Project>>) {
    _projects.observe(owner, observer)
  }
}
