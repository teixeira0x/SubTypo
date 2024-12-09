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
import com.teixeira0x.subtypo.domain.model.ProjectData
import com.teixeira0x.subtypo.domain.usecase.project.GetProjectDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ProjectViewModel
@Inject
constructor(private val getProjectDataUseCase: GetProjectDataUseCase) :
  ViewModel() {

  private val _state = MutableLiveData<ProjectState>(ProjectState.Loading)

  val stateData: LiveData<ProjectState>
    get() = _state

  fun loadProjectData(id: Long) {
    viewModelScope.launch {
      getProjectDataUseCase(id).collect { projectData ->
        _state.postValue(
          if (projectData != null) {
            ProjectState.Loaded(projectData)
          } else ProjectState.Error(R.string.proj_error_unable_to_load)
        )
      }
    }
  }

  sealed interface ProjectState {
    object Loading : ProjectState

    class Loaded(val projectData: ProjectData) : ProjectState

    class Error(val message: Int) : ProjectState
  }
}
