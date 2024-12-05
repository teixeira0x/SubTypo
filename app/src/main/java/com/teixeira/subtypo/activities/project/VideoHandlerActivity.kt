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

package com.teixeira.subtypo.activities.project

import android.os.Bundle
import android.widget.SeekBar
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_AVAILABLE_COMMANDS_CHANGED
import androidx.media3.common.Player.EVENT_IS_PLAYING_CHANGED
import androidx.media3.common.Player.EVENT_PLAYBACK_STATE_CHANGED
import androidx.media3.common.Player.EVENT_PLAY_WHEN_READY_CHANGED
import androidx.media3.common.Player.Events
import androidx.media3.exoplayer.ExoPlayer
import com.blankj.utilcode.util.ThreadUtils
import com.teixeira.subtypo.R
import com.teixeira.subtypo.subtitle.utils.TimeUtils.getFormattedTime

/**
 * Base class for ProjectActivity that handles most video related things.
 *
 * @author Felipe Teixeira
 */
abstract class VideoHandlerActivity : ProjectHandlerActivity() {

  companion object {
    const val MAX_UPDATE_INTERVAL_MS = 1_000L
  }

  private val handler = ThreadUtils.getMainHandler()
  private var updateProgressAction: Runnable? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    updateProgressAction = Runnable { updateProgress() }

    binding.videoContent.videoView.setUri(project.videoPath)
    binding.videoContent.videoView.seekTo(videoViewModel.currentPosition)
    setVideoViewModelObservers()
    configureListeners()
  }

  override fun onPause() {
    super.onPause()

    videoViewModel.pauseVideo()
  }

  override fun preDestroy() {
    super.preDestroy()

    binding.videoContent.videoView.release()
    videoViewModel.isPrepared = false
  }

  override fun postDestroy() {
    super.postDestroy()
    updateProgressAction?.also { handler.removeCallbacks(it) }
    updateProgressAction = null
  }

  private fun configureListeners() {
    binding.videoContent.videoView.addListener(
      object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
          when (state) {
            Player.STATE_READY -> onVideoPrepared(binding.videoContent.videoView.player)
            Player.STATE_ENDED -> {
              binding.controllerContent.play.setImageResource(R.drawable.ic_play)
              videoViewModel.setCurrentPosition(0, true)
              videoViewModel.pauseVideo()
            }
            else -> {}
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
          )
            updateProgress()
        }
      }
    )

    binding.controllerContent.seekBar.setOnSeekBarChangeListener(
      object : SeekBar.OnSeekBarChangeListener {

        private var arePlaying = false

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          if (fromUser) videoViewModel.setCurrentPosition(progress.toLong(), true)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
          arePlaying = videoViewModel.isPlaying
          if (arePlaying) videoViewModel.pauseVideo()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
          if (arePlaying) videoViewModel.playVideo()
        }
      }
    )

    binding.controllerContent.play.setOnClickListener {
      videoViewModel.isPlaying = !videoViewModel.isPlaying
    }

    binding.controllerContent.undo.setOnClickListener { subtitlesViewModel.undo() }
    binding.controllerContent.redo.setOnClickListener { subtitlesViewModel.redo() }
    binding.controllerContent.skipBackward.setOnClickListener {
      binding.videoContent.videoView.seekBackward()
    }
    binding.controllerContent.skipFoward.setOnClickListener {
      binding.videoContent.videoView.seekFoward()
    }

    binding.controllerContent.changeSubtitle.setOnClickListener { showSubtitleSelectorDialog() }
    binding.controllerContent.addParagraph.setOnClickListener {
      videoViewModel.pauseVideo()
      if (subtitlesViewModel.subtitles.isEmpty()) {
        showSubtitleEditorSheet()
        return@setOnClickListener
      }
      showParagraphEditorSheet()
    }
  }

  protected fun updateVideoUI(currentPosition: Long) {
    requireParagraphListAdapter().setVideoPosition(currentPosition)
    binding.controllerContent.currentVideoPosition.setText(currentPosition.getFormattedTime())
    binding.controllerContent.seekBar.setProgress(currentPosition.toInt())
    binding.videoContent.subtitleView.setVideoPosition(currentPosition)
    binding.timeLine.setCurrentPosition(currentPosition)
  }

  private fun onVideoPrepared(player: ExoPlayer) {
    projectManager.updateVideoFormat(player.videoFormat)

    if (videoViewModel.isPrepared.not()) { // The first preparation of the video
      val duration = player.duration

      binding.controllerContent.videoDuration.setText(duration.getFormattedTime())
      binding.controllerContent.seekBar.setMax(duration.toInt())
      binding.timeLine.setDuration(duration)
      videoViewModel.duration = duration
      videoViewModel.isPrepared = true

      updateVideoUI(videoViewModel.currentPosition)
    }
  }

  private fun setVideoViewModelObservers() {
    videoViewModel.observeIsPrepared(this) { isPrepared ->
      binding.controllerContent.skipBackward.isEnabled = isPrepared
      binding.controllerContent.play.isEnabled = isPrepared
      binding.controllerContent.skipFoward.isEnabled = isPrepared
      binding.controllerContent.changeSubtitle.isEnabled = isPrepared
      binding.controllerContent.addParagraph.isEnabled = isPrepared
    }

    videoViewModel.observeCurrentPosition(this) { (currentPosition, seekTo) ->
      if (seekTo) binding.videoContent.videoView.seekTo(currentPosition)
      updateVideoUI(currentPosition)
    }

    videoViewModel.observeIsPlaying(this) { isPlaying ->
      if (isDestroying) {
        return@observeIsPlaying
      }
      binding.controllerContent.play.setImageResource(
        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
      )
      binding.videoContent.videoView.setPlaying(isPlaying)
      requireParagraphListAdapter().isVideoPlaying = isPlaying
    }
  }

  private fun updateProgress() {
    if (isDestroying) {
      return
    }

    val updateProgressAction = updateProgressAction!!
    val player = binding.videoContent.videoView.player

    videoViewModel.setCurrentPosition(player.currentPosition, false)

    handler.removeCallbacks(updateProgressAction)
    if (player.isPlaying) {
      handler.post(updateProgressAction)
    }
  }
}
