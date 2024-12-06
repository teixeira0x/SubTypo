package com.teixeira0x.subtypo.ui.activity.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.teixeira0x.subtypo.R

class PreferencesViewModel : ViewModel() {

  private val _currentPreferencesId = MutableLiveData<Int>(R.xml.preferences)

  val currentPreferencesIdData: LiveData<Int>
    get() = _currentPreferencesId

  var currentPreferencesId: Int
    get() = _currentPreferencesId.value!!
    set(value) {
      _currentPreferencesId.value = value
    }
}
