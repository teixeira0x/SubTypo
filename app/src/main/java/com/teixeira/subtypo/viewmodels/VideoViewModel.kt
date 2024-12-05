package com.teixeira.subtypo.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

class VideoViewModel : ViewModel() {

  private val _isPrepared = MutableLiveData<Boolean>(false)
  private val _currentPosition = MutableLiveData<Pair<Long, Boolean>>(0.toLong() to false)
  private val _duration = MutableLiveData<Long>(0)
  private val _isPlaying = MutableLiveData<Boolean>(false)

  var isPrepared: Boolean
    get() = _isPrepared.value!!
    set(value) {
      _isPrepared.value = value
    }

  val currentPosition: Long
    get() = _currentPosition.value!!.first

  var duration: Long
    get() = _duration.value!!
    set(value) {
      _duration.value = value
    }

  var isPlaying: Boolean
    get() = _isPlaying.value!!
    set(value) {
      _isPlaying.value = value
    }

  fun setCurrentPosition(position: Long, seekTo: Boolean) {
    _currentPosition.value = position to seekTo
  }

  fun playVideo() {
    isPlaying = true
  }

  fun pauseVideo() {
    isPlaying = false
  }

  fun observeIsPrepared(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
    _isPrepared.observe(lifecycleOwner, observer)
  }

  fun observeCurrentPosition(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<Pair<Long, Boolean>>,
  ) {
    _currentPosition.observe(lifecycleOwner, observer)
  }

  fun observeIsPlaying(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
    _isPlaying.observe(lifecycleOwner, observer)
  }
}
