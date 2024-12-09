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

class VideoViewModel : ViewModel() {

  private val _videoState = MutableLiveData<VideoState>()
  val videoState: LiveData<VideoState>
    get() = _videoState

  private val playerPosition = MutableLiveData<Long>(0L)

  fun onVideoReady() {
    _videoState.value = VideoState.Ready
  }

  fun onVideoEnded() {
    _videoState.value = VideoState.Ended
  }

  fun onUpdateProgress(currentPosition: Long) {
    playerPosition.value = currentPosition
    _videoState.value = VideoState.Playing(currentPosition)
  }

  sealed interface VideoState {
    object Ready : VideoState

    data class Playing(val currentPosition: Long) : VideoState

    object Ended : VideoState
  }
}
