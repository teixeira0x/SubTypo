package com.teixeira0x.subtypo.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

  companion object {
    const val FRAGMENT_PROJECTS_INDEX = 0
    const val FRAGMENT_SETTINGS_INDEX = 1
  }

  private val _currentFragmentIndex = MutableLiveData<Int>(FRAGMENT_PROJECTS_INDEX)

  var currentFragmentIndex: Int
    get() = _currentFragmentIndex.value!!
    set(value) {
      _currentFragmentIndex.value = value
    }

  fun observeCurrentFragmentIndex(owner: LifecycleOwner, observer: Observer<Int>) {
    _currentFragmentIndex.observe(owner, observer)
  }
}
