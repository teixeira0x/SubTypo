package com.teixeira.subtitles.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

class ProjectEditorViewModel : ViewModel() {

  private val _isCreatingProject = MutableLiveData<Boolean>(false)

  var isCreatingProject: Boolean
    get() = _isCreatingProject.value!!
    set(value) {
      _isCreatingProject.value = value
    }

  fun observeCreatingProject(owner: LifecycleOwner, observer: Observer<Boolean>) {
    _isCreatingProject.observe(owner, observer)
  }
}
