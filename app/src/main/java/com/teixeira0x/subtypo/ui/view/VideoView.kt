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

package com.teixeira0x.subtypo.ui.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.teixeira0x.subtypo.R

/**
 * A simple implementation for video viewer using ExoPlayer.
 *
 * @author Felipe Teixeira
 */
class VideoView
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

  companion object {
    private const val HORIZONTAL_ASPECT_RATIO = 1.7777778f

    private const val DEFAULT_SEEK_BACK_MS = 5_000L

    private const val DEFAULT_SEEK_FOWARD_MS = 5_000L
  }

  private val componentListener = ComponentListener()
  private val contentFrame = AspectRatioFrameLayout(context)
  private val surfaceView = SurfaceView(context)
  private val _player =
    ExoPlayer.Builder(context)
      .setSeekBackIncrementMs(DEFAULT_SEEK_BACK_MS)
      .setSeekForwardIncrementMs(DEFAULT_SEEK_FOWARD_MS)
      .build()

  init {
    contentFrame.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
    contentFrame.setAspectRatio(LANDSCAPE_ASPECT_RATIO)
    contentFrame.addView(surfaceView, 0)
    addView(contentFrame)

    _player.addListener(componentListener)
    _player.setVideoSurfaceView(surfaceView)
  }

  val player: ExoPlayer
    get() = _player

  val isPlaying: Boolean
    get() = player.isPlaying()

  val currentPosition: Long
    get() = player.currentPosition

  val videoFormat: Format?
    get() = player.videoFormat

  fun setUri(uri: Uri) = setMedia(MediaItem.fromUri(uri))

  fun setUri(uri: String) = setMedia(MediaItem.fromUri(uri))

  fun setMedia(media: MediaItem) = prepareMedia(media)

  fun addListener(listener: Player.Listener) = player.addListener(listener)

  fun play() = player.play()

  fun pause() = player.pause()

  fun stop() = player.stop()

  fun seekTo(position: Long) = player.seekTo(position)

  fun seekBackward() = player.seekBack()

  fun seekFoward() = player.seekForward()

  fun release() = player.release()

  private fun prepareMedia(media: MediaItem) {
    player.clearMediaItems()
    player.setMediaItem(media)
    player.prepare()
  }

  private inner class ComponentListener : Player.Listener {

    override fun onVideoSizeChanged(videoSize: VideoSize) {

      val width = videoSize.width
      val height = videoSize.height
      val videoAspectRatio: Float =
        if (height == 0 || width == 0) 0F
        else (width * videoSize.pixelWidthHeightRatio) / height

      if (videoAspectRatio >= HORIZONTAL_ASPECT_RATIO) {
        contentFrame.setAspectRatio(HORIZONTAL_ASPECT_RATIO)

        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
      } else {
        contentFrame.setAspectRatio(videoAspectRatio)

        layoutParams.height =
          context.resources.getDimensionPixelSize(R.dimen.video_view_height_max)
      }
    }
  }
}
