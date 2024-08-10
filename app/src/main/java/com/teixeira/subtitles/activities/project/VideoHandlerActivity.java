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

package com.teixeira.subtitles.activities.project;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.media3.common.Player;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.teixeira.subtitles.fragments.sheets.SubtitleEditorSheetFragment;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.preferences.Preferences;
import com.teixeira.subtitles.utils.UiUtils;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.utils.VideoUtils;
import java.util.List;

/**
 * Base class for ProjectActivity that handles most video related things.
 *
 * @author Felipe Teixeira
 */
abstract class VideoHandlerActivity extends BaseProjectActivity {

  private Runnable progressTracker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    progressTracker = this::progressTracker;

    if (!Preferences.isDevelopmentUndoAndRedoEnabled()) {
      binding.controllerContent.redo.setVisibility(View.GONE);
      binding.controllerContent.undo.setVisibility(View.GONE);
    }
    subtitlesViewModel.setUndoManagerEnabled(Preferences.isDevelopmentUndoAndRedoEnabled());

    binding.videoContent.videoView.setVideoPath(project.getVideoPath());
    binding.videoContent.videoView.seekTo(videoViewModel.getCurrentPosition());
    setVideoViewModelObservers();
  }

  @Override
  protected void onPause() {
    super.onPause();

    videoViewModel.pauseVideo();
  }

  @Override
  protected void preDestroy() {
    super.preDestroy();

    binding.videoContent.videoView.release();
    videoViewModel.setPrepared(false);
  }

  @Override
  protected void configureListeners() {
    super.configureListeners();
    binding.videoContent.videoView.setPlayerListener(
        new Player.Listener() {
          @Override
          public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_READY) {
              onVideoPrepared(binding.videoContent.videoView.getPlayer());
            } else if (state == Player.STATE_ENDED) {
              binding.controllerContent.play.setImageResource(R.drawable.ic_play);
            }
          }

          @Override
          public void onPositionDiscontinuity(
              Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
            videoViewModel.setCurrentPosition(newPosition.contentPositionMs, false);
          }

          @Override
          public void onIsPlayingChanged(boolean isPlaying) {
            if (isPlaying && progressTracker != null) {
              ThreadUtils.getMainHandler().post(progressTracker);
            }
          }
        });

    binding.controllerContent.seekBar.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {

          private boolean wasPlaying;

          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
              videoViewModel.setCurrentPosition(progress, true);
            }
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
            wasPlaying = videoViewModel.isPlaying();
            if (wasPlaying) videoViewModel.pauseVideo();
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
            if (wasPlaying) videoViewModel.playVideo();
          }
        });

    binding.controllerContent.play.setOnClickListener(
        v -> videoViewModel.setPlaying(!videoViewModel.isPlaying()));

    binding.controllerContent.undo.setOnClickListener(v -> subtitlesViewModel.undo());
    binding.controllerContent.redo.setOnClickListener(v -> subtitlesViewModel.redo());
    binding.controllerContent.skipBackward.setOnClickListener(
        v -> binding.videoContent.videoView.seekBackward());
    binding.controllerContent.skipFoward.setOnClickListener(
        v -> binding.videoContent.videoView.seekFoward());
    binding.controllerContent.addSubtitle.setOnClickListener(
        v -> {
          videoViewModel.pauseVideo();
          SubtitleEditorSheetFragment.newInstance(videoViewModel.getCurrentPosition())
              .show(getSupportFragmentManager(), null);
        });
  }

  protected void updateVideoUI(long currentPosition) {
    binding.controllerContent.currentVideoPosition.setText(VideoUtils.getTime(currentPosition));
    binding.controllerContent.seekBar.setProgress((int) currentPosition);
    binding.timeLine.setCurrentPosition(currentPosition);

    List<Subtitle> subtitles = subtitlesViewModel.getSubtitles();
    boolean subtitleFound = false;
    for (int i = 0; i < subtitles.size(); i++) {
      Subtitle subtitle = subtitles.get(i);
      long startTime = VideoUtils.getMilliSeconds(subtitle.getStartTime());
      long endTime = VideoUtils.getMilliSeconds(subtitle.getEndTime());

      if (currentPosition >= startTime && currentPosition <= endTime) {
        binding.videoContent.subtitleView.setSubtitle(subtitle);
        binding.videoContent.subtitleView.setVisibility(View.VISIBLE);
        subtitlesViewModel.setVideoSubtitleIndex(i);
        subtitleFound = true;
        break;
      }
    }

    if (!subtitleFound) {
      binding.videoContent.subtitleView.setVisibility(View.GONE);
      subtitlesViewModel.setVideoSubtitleIndex(-1);
    }
  }

  private void onVideoPrepared(Player player) {
    long duration = player.getDuration();

    binding.controllerContent.videoDuration.setText(VideoUtils.getTime(duration));
    binding.controllerContent.seekBar.setMax((int) duration);
    binding.timeLine.setDuration(duration);
    videoViewModel.setDuration(duration);

    int width = player.getVideoSize().width;
    int height = player.getVideoSize().height;

    if (width > height) {
      binding.videoContent.videoView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
      binding.videoContent.videoView.requestLayout();
    } else if (height > width
        && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
      binding.videoContent.getRoot().getLayoutParams().height = SizeUtils.dp2px(350f);
      binding.videoContent.getRoot().requestLayout();
    }
    
    updateVideoUI(videoViewModel.getCurrentPosition());
    videoViewModel.setPrepared(true);
  }

  private void setVideoViewModelObservers() {
    videoViewModel.observeIsPrepared(
        this,
        isPrepared -> {
          UiUtils.setImageEnabled(binding.controllerContent.skipBackward, isPrepared);
          UiUtils.setImageEnabled(binding.controllerContent.play, isPrepared);
          UiUtils.setImageEnabled(binding.controllerContent.skipFoward, isPrepared);
          UiUtils.setImageEnabled(binding.controllerContent.addSubtitle, isPrepared);
        });
    videoViewModel.observeCurrentPosition(
        this,
        currentPositionPair -> {
          long currentVideoPosition = currentPositionPair.first;
          boolean seekTo = currentPositionPair.second;
          if (seekTo) {
            binding.videoContent.videoView.seekTo(currentVideoPosition);
          }
          updateVideoUI(currentVideoPosition);
        });

    videoViewModel.observeIsPlaying(
        this,
        isPlaying -> {
          if (isDestroying()) {
            return;
          }
          binding.controllerContent.play.setImageResource(
              isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
          binding.videoContent.videoView.setPlaying(isPlaying);
          requireSubtitleListAdapter().setVideoPlaying(isPlaying);
        });
  }

  private void progressTracker() {

    if (isDestroying()) {
      return;
    }

    videoViewModel.setCurrentPosition(binding.videoContent.videoView.getCurrentPosition(), false);

    if (videoViewModel.isPlaying() && progressTracker != null) {
      ThreadUtils.getMainHandler().post(progressTracker);
    }
  }
}
