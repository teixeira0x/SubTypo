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

package com.teixeira0x.subtypo.ui.activity.project.player

import android.widget.SeekBar
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_AVAILABLE_COMMANDS_CHANGED
import androidx.media3.common.Player.EVENT_IS_PLAYING_CHANGED
import androidx.media3.common.Player.EVENT_PLAYBACK_STATE_CHANGED
import androidx.media3.common.Player.EVENT_PLAY_WHEN_READY_CHANGED
import androidx.media3.common.Player.Events
import com.blankj.utilcode.util.ThreadUtils
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.databinding.LayoutPlayerContentBinding
import com.teixeira0x.subtypo.databinding.LayoutPlayerControllerContentBinding
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.VideoViewModel
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.VideoViewModel.VideoState
import com.teixeira0x.subtypo.utils.TimeUtils.getFormattedTime

class PlayerControlLayoutHandler(
  private val playerBinding: LayoutPlayerContentBinding,
  private val playerControllerBinding: LayoutPlayerControllerContentBinding,
  private val viewModel: VideoViewModel,
) : DefaultLifecycleObserver {

  private val handler = ThreadUtils.getMainHandler()
  private var updateProgressAction: Runnable? = Runnable { updateProgress() }

  override fun onCreate(owner: LifecycleOwner) {
    viewModel.videoState.observe(owner) { state ->
      when (state) {
        is VideoViewModel.VideoState.Ready -> onVideoReady()
        is VideoViewModel.VideoState.Playing ->
          onUpdateVideoPosition(state.currentPosition)
        is VideoViewModel.VideoState.Ended -> onVideoEnded()
      }
    }
    configureListeners()
  }

  override fun onDestroy(owner: LifecycleOwner) {
    if (playerBinding.videoView.isPlaying) {
      playerBinding.videoView.stop()
    }
    playerBinding.videoView.release()

    updateProgressAction?.let { handler.removeCallbacks(it) }
    updateProgressAction = null
  }

  override fun onPause(owner: LifecycleOwner) {
    if (playerBinding.videoView.isPlaying) {
      playerBinding.videoView.pause()
    }
  }

  private fun configureListeners() {
    playerBinding.videoView.addListener(
      object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
          when (state) {
            Player.STATE_READY -> viewModel.onVideoReady()
            Player.STATE_ENDED -> viewModel.onVideoEnded()
          }
        }

        override fun onEvents(player: Player, events: Events) {
          if (
            events.containsAny(
              EVENT_PLAYBACK_STATE_CHANGED,
              EVENT_PLAY_WHEN_READY_CHANGED,
              EVENT_IS_PLAYING_CHANGED,
              EVENT_AVAILABLE_COMMANDS_CHANGED,
            )
          ) {
            updateProgress()
          }
        }
      }
    )

    playerControllerBinding.play.setOnClickListener {
      if (playerBinding.videoView.isPlaying) {
        playerControllerBinding.play.setImageResource(R.drawable.ic_play)
        playerBinding.videoView.pause()
      } else {
        playerControllerBinding.play.setImageResource(R.drawable.ic_pause)
        playerBinding.videoView.play()
      }
    }

    playerControllerBinding.skipBackward.setOnClickListener {
      playerBinding.videoView.seekBackward()
    }
    playerControllerBinding.skipFoward.setOnClickListener {
      playerBinding.videoView.seekFoward()
    }

    playerControllerBinding.seekBar.setOnSeekBarChangeListener(
      object : SeekBar.OnSeekBarChangeListener {
        private var isPlaying = false

        override fun onProgressChanged(
          seekBar: SeekBar,
          progress: Int,
          fromUser: Boolean,
        ) {
          if (fromUser) playerBinding.videoView.seekTo(progress.toLong())
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
          isPlaying = playerBinding.videoView.isPlaying
          if (isPlaying) playerBinding.videoView.pause()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
          if (isPlaying) playerBinding.videoView.play()
        }
      }
    )
  }

  private fun onVideoReady() {
    val duration = playerBinding.videoView.player.duration
    playerControllerBinding.videoDuration.setText(duration.getFormattedTime())
    playerControllerBinding.seekBar.max = duration.toInt()
    onUpdateVideoPosition(0L)
  }

  private fun onUpdateVideoPosition(currentPosition: Long) {
    playerControllerBinding.currentVideoPosition.setText(
      currentPosition.getFormattedTime()
    )
    playerControllerBinding.seekBar.progress = currentPosition.toInt()
  }

  private fun onVideoEnded() {
    playerControllerBinding.play.setImageResource(R.drawable.ic_play)
    playerBinding.videoView.pause()
    onUpdateVideoPosition(0L)
  }

  private fun updateProgress() {
    val player = playerBinding.videoView.player
    viewModel.onUpdateProgress(player.currentPosition)

    if (player.isPlaying) {
      updateProgressAction?.let { handler.post(it) }
    }
  }
}
