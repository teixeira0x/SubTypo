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

package com.teixeira.subtitles.activities.project

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.media3.common.Player
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ThreadUtils
import com.teixeira.subtitles.R
import com.teixeira.subtitles.fragments.dialogs.ParagraphEditorDialogFragment
import com.teixeira.subtitles.subtitle.utils.TimeUtils
import com.teixeira.subtitles.utils.UiUtils

/**
 * Base class for ProjectActivity that handles most video related things.
 *
 * @author Felipe Teixeira
 */
abstract class VideoHandlerActivity : ProjectHandlerActivity() {

  private var progressTracker: Runnable? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    progressTracker = Runnable { progressTracker() }

    binding.videoContent.videoView.setVideoPath(project.videoPath)
    binding.videoContent.videoView.seekTo(videoViewModel.currentPosition)
    setVideoViewModelObservers()
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

  override fun configureListeners() {
    super.configureListeners()
    binding.videoContent.videoView.setPlayerListener(
      object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
          if (state == Player.STATE_READY) {
            onVideoPrepared(binding.videoContent.videoView.player!!)
          } else if (state == Player.STATE_ENDED) {
            binding.controllerContent.play.setImageResource(R.drawable.ic_play)
            videoViewModel.setCurrentPosition(0, true)
            videoViewModel.pauseVideo()
          }
        }

        override fun onPositionDiscontinuity(
          oldPosition: Player.PositionInfo,
          newPosition: Player.PositionInfo,
          reason: Int,
        ) {
          videoViewModel.setCurrentPosition(newPosition.contentPositionMs, false)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
          if (isPlaying) {
            progressTracker?.also { ThreadUtils.getMainHandler().post(it) }
          }
        }
      }
    )

    binding.controllerContent.seekBar.setOnSeekBarChangeListener(
      object : SeekBar.OnSeekBarChangeListener {

        private var wasPlaying = false

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          if (fromUser) videoViewModel.setCurrentPosition(progress.toLong(), true)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
          wasPlaying = videoViewModel.isPlaying
          if (wasPlaying) videoViewModel.pauseVideo()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
          if (wasPlaying) videoViewModel.playVideo()
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

    binding.controllerContent.changeLanguage.setOnClickListener { showSubtitleSelectorDialog() }
    binding.controllerContent.addParagraph.setOnClickListener {
      videoViewModel.pauseVideo()
      if (subtitlesViewModel.subtitles.isEmpty()) {
        showSubtitleEditorDialog()
        return@setOnClickListener
      }
      ParagraphEditorDialogFragment.newInstance(videoViewModel.currentPosition)
        .show(supportFragmentManager, null)
    }
  }

  protected fun updateVideoUI(currentPosition: Long) {
    requireParagraphListAdapter().setVideoPosition(currentPosition)
    binding.controllerContent.currentVideoPosition.setText(
      TimeUtils.getFormattedTime(currentPosition)
    )
    binding.videoContent.subtitleView.setVideoPosition(currentPosition)
    binding.controllerContent.seekBar.setProgress(currentPosition.toInt())
    binding.timeLine.setCurrentPosition(currentPosition)
  }

  private fun onVideoPrepared(player: Player) {
    val duration = player.duration

    binding.controllerContent.videoDuration.setText(TimeUtils.getFormattedTime(duration))
    binding.controllerContent.seekBar.setMax(duration.toInt())
    binding.timeLine.setDuration(duration)
    videoViewModel.duration = duration

    val width = player.videoSize.width
    val height = player.videoSize.height

    when {
      width > height -> {
        binding.videoContent.videoView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT
        binding.videoContent.videoView.requestLayout()
      }
      height > width &&
        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT -> {
        binding.videoContent.root.getLayoutParams().height = SizeUtils.dp2px(350f)
        binding.videoContent.root.requestLayout()
      }
      else -> {}
    }

    updateVideoUI(videoViewModel.currentPosition)
    videoViewModel.isPrepared = true
  }

  private fun setVideoViewModelObservers() {
    videoViewModel.observeIsPrepared(this) { isPrepared ->
      UiUtils.setImageEnabled(binding.controllerContent.skipBackward, isPrepared)
      UiUtils.setImageEnabled(binding.controllerContent.play, isPrepared)
      UiUtils.setImageEnabled(binding.controllerContent.skipFoward, isPrepared)
      UiUtils.setImageEnabled(binding.controllerContent.addParagraph, isPrepared)
    }
    videoViewModel.observeCurrentPosition(this) { (currentPosition, seekTo) ->
      if (seekTo) {
        binding.videoContent.videoView.seekTo(currentPosition)
      }
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

  private fun progressTracker() {

    if (isDestroying) {
      return
    }

    videoViewModel.setCurrentPosition(binding.videoContent.videoView.getCurrentPosition(), false)

    if (videoViewModel.isPlaying) {
      progressTracker?.also { ThreadUtils.getMainHandler().post(it) }
    }
  }
}
