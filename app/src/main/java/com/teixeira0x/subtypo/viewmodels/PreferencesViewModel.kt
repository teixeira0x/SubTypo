package com.teixeira0x.subtypo.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.teixeira0x.subtypo.R

class PreferencesViewModel : ViewModel() {

  private val _currentPreferencesId = MutableLiveData<Int>(R.xml.preferences)

  var currentPreferencesId: Int
    get() = _currentPreferencesId.value!!
    set(value) {
      _currentPreferencesId.value = value
    }

  fun observeCurrentPreferencesId(
    owner: LifecycleOwner,
    observer: Observer<Int>,
  ) {
    _currentPreferencesId.observe(owner, observer)
  }
}
