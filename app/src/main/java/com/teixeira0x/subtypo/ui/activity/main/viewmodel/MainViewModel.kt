package com.teixeira0x.subtypo.ui.activity.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

  companion object {
    const val FRAGMENT_PROJECTS_INDEX = 0
    const val FRAGMENT_SETTINGS_INDEX = 1
  }

  private val _currentFragmentIndex =
    MutableLiveData<Int>(FRAGMENT_PROJECTS_INDEX)

  val currentFragmentIndexData: LiveData<Int>
    get() = _currentFragmentIndex

  var currentFragmentIndex: Int
    get() = _currentFragmentIndex.value!!
    set(value) {
      _currentFragmentIndex.value = value
    }
}
