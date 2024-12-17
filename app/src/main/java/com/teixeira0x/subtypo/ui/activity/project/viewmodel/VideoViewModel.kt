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
import com.teixeira0x.subtypo.domain.model.Cue

class VideoViewModel : ViewModel() {

  private val _videoEvent = MutableLiveData<VideoEvent>()
  val videoEvent: LiveData<VideoEvent>
    get() = _videoEvent

  private val _videoUri = MutableLiveData<String>("")
  val videoUriData: LiveData<String>
    get() = _videoUri

  private val _videoPosition = MutableLiveData<Long>(0L)
  val videoPosition: Long
    get() = _videoPosition.value!!

  private val _cues = MutableLiveData<List<Cue>>(emptyList())
  val cuesData: LiveData<List<Cue>>
    get() = _cues

  private val _isPlayerVisible = MutableLiveData<Boolean>(true)
  val isPlayerVisibleData: LiveData<Boolean>
    get() = _isPlayerVisible

  fun doEvent(event: VideoEvent) {
    _videoEvent.value = event
  }

  fun loadVideo(videoUri: String) {
    _videoUri.value = videoUri
    if (videoUri.isEmpty()) {
      _isPlayerVisible.value = false
    }
  }

  fun updateCues(cues: List<Cue>) {
    _cues.value = cues
  }

  fun setPlayerVisible(visible: Boolean) {
    _isPlayerVisible.value = visible
  }

  fun saveVideoPosition(videoPosition: Long) {
    _videoPosition.value = videoPosition
  }

  sealed class VideoEvent {
    data object Pause : VideoEvent()

    data object Play : VideoEvent()
  }
}
