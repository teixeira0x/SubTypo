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

package com.teixeira0x.subtypo.ui.activity.project.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_AVAILABLE_COMMANDS_CHANGED
import androidx.media3.common.Player.EVENT_IS_PLAYING_CHANGED
import androidx.media3.common.Player.EVENT_PLAYBACK_STATE_CHANGED
import androidx.media3.common.Player.EVENT_PLAY_WHEN_READY_CHANGED
import androidx.media3.common.Player.Events
import androidx.media3.common.VideoSize
import androidx.media3.common.text.Cue.Builder
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecRenderer.DecoderInitializationException
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil.DecoderQueryException
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.databinding.FragmentPlayerBinding
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.VideoViewModel
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.VideoViewModel.VideoEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerFragment : Fragment() {

  companion object {
    private const val HORIZONTAL_ASPECT_RATIO = 1.7770000f
    private const val DEFAULT_SEEK_BACK_MS = 5_000L
    private const val DEFAULT_SEEK_FOWARD_MS = 5_000L
  }

  private val mainHandler = Handler(Looper.getMainLooper())

  private val videoViewModel by
    viewModels<VideoViewModel>(ownerProducer = { requireActivity() })

  private var _binding: FragmentPlayerBinding? = null
  private val binding: FragmentPlayerBinding
    get() = checkNotNull(_binding) { "VidePlayerFragment has been destroyed" }

  private var updateSubtitleAction: Runnable? = null
  private var player: ExoPlayer? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentPlayerBinding.inflate(inflater, container, false)
      .also { _binding = it }
      .root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    releasePlayer()
    _binding = null
  }

  override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
    binding.playerView.setErrorMessageProvider(PlayerErrorMessageProvider())
    binding.playerView.controllerShowTimeoutMs = 2000
    binding.playerView.useController = true
    observeViewModel()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    saveVideoPosition()
  }

  override fun onStart() {
    super.onStart()
    initializePlayer()
    _binding?.playerView?.onResume()
  }

  override fun onStop() {
    super.onStop()
    _binding?.playerView?.onPause()
    releasePlayer()
  }

  private fun observeViewModel() {
    videoViewModel.videoEvent.observe(this) { event ->
      when (event) {
        is VideoEvent.Play -> playVideo()
        is VideoEvent.Pause -> pauseVideo()
      }
    }

    videoViewModel.videoUriData.observe(this) { videoUri ->
      prepareMedia(videoUri)
    }
    videoViewModel.cuesData.observe(this) { updateSubtitle() }
    videoViewModel.isPlayerVisibleData.observe(this) { visible ->
      if (visible) {
        initializePlayer()
      } else releasePlayer()
    }
  }

  private fun initializePlayer() {
    if (player == null) {
      updateSubtitleAction = Runnable { updateSubtitle() }

      _binding?.playerView?.player =
        ExoPlayer.Builder(requireContext())
          .setSeekBackIncrementMs(DEFAULT_SEEK_BACK_MS)
          .setSeekForwardIncrementMs(DEFAULT_SEEK_FOWARD_MS)
          .build()
          .also { player = it }
      player?.addListener(Listener())
    }

    prepareMedia(videoViewModel.videoUriData.value!!)
  }

  private fun releasePlayer() {
    updateSubtitleAction?.let { mainHandler.removeCallbacks(it) }
    updateSubtitleAction = null
    saveVideoPosition()
    player?.release()
    player = null
  }

  private fun prepareMedia(videoUri: String) {
    player?.apply {
      clearMediaItems()
      setMediaItem(MediaItem.fromUri(videoUri))
      prepare()

      seekTo(videoViewModel.videoPosition)
    }
  }

  private fun saveVideoPosition() {
    player?.let { videoViewModel.saveVideoPosition(it.currentPosition) }
  }

  private fun playVideo() {
    player?.play()
  }

  private fun pauseVideo() {
    player?.pause()
    saveVideoPosition()
  }

  private fun updateSubtitle() {
    val player = player
    val cues = videoViewModel.cuesData.value!!
    if (cues.isEmpty() || player == null) {
      return
    }

    val currentPosition = player.currentPosition
    val filteredCues =
      cues.filter {
        it.startTime <= currentPosition && it.endTime >= currentPosition
      }

    binding.playerView.subtitleView?.setCues(
      filteredCues.map { Builder().setText(it.text).build() }
    )

    if (player.isPlaying()) {
      updateSubtitleAction?.let { mainHandler.post(it) }
    }
  }

  inner class Listener : Player.Listener {

    override fun onVideoSizeChanged(videoSize: VideoSize) {
      val orientation = resources.configuration.orientation
      if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
        val width = videoSize.width
        val height = videoSize.height
        val videoAspectRatio: Float =
          if (height == 0 || width == 0) 0F
          else (width * videoSize.pixelWidthHeightRatio) / height

        if (videoAspectRatio <= HORIZONTAL_ASPECT_RATIO) {
          _binding?.root?.apply {
            layoutParams.height =
              resources.getDimensionPixelSize(R.dimen.video_view_height_max)
            requestLayout()
          }
        }
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
        updateSubtitleAction?.let { mainHandler.post(it) }
      }
    }
  }

  inner class PlayerErrorMessageProvider :
    ErrorMessageProvider<PlaybackException> {

    override fun getErrorMessage(e: PlaybackException): Pair<Int, String> {
      var errorString = getString(R.string.video_player_error_generic)
      val cause = e.cause

      if (cause is DecoderInitializationException) {
        val codecInfo = cause.codecInfo
        errorString =
          if (codecInfo == null) {
            // Special case for decoder initialization failures.
            when {
              cause.cause is DecoderQueryException -> {
                getString(R.string.video_player_error_querying_decoders)
              }
              cause.secureDecoderRequired -> {
                getString(
                  R.string.video_player_error_no_secure_decoder,
                  cause.mimeType,
                )
              }
              else -> {
                getString(
                  R.string.video_player_error_no_decoder,
                  cause.mimeType,
                )
              }
            }
          } else {
            getString(
              R.string.video_player_error_instantiating_decoder,
              codecInfo.name,
            )
          }
      }

      return Pair.create(0, errorString)
    }
  }
}
/*
inner class PlayerErrorMessageProvider :
    ErrorMessageProvider<PlaybackException> {

  @OptIn(UnstableApi::class) // Using decoder exceptions
  override fun getErrorMessage(e: PlaybackException): Pair<Int, String> {
    var errorString = getString(R.string.video_player_error_generic)
    val cause = e.cause

    if (cause is DecoderInitializationException) {
      // Armazena codecInfo em uma variável local para evitar problemas de smart cast
      val codecInfo = cause.codecInfo

      errorString = if (codecInfo == null) {
        when {
          cause.cause is DecoderQueryException -> {
            getString(R.string.video_player_error_querying_decoders)
          }
          cause.secureDecoderRequired -> {
            getString(
              R.string.video_player_error_no_secure_decoder,
              cause.mimeType,
            )
          }
          else -> {
            getString(
              R.string.video_player_error_no_decoder,
              cause.mimeType,
            )
          }
        }
      } else {
        getString(
          R.string.video_player_error_instantiating_decoder,
          codecInfo.name,
        )
      }
    }

    return 0 to errorString
  }
}*/
