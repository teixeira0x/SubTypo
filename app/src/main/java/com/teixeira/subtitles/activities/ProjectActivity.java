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

package com.teixeira.subtitles.activities;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.os.BundleCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.Player;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.adapters.SubtitleListAdapter;
import com.teixeira.subtitles.databinding.ActivityProjectBinding;
import com.teixeira.subtitles.fragments.sheets.SubtitleEditorSheetFragment;
import com.teixeira.subtitles.managers.UndoManager;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.preferences.Preferences;
import com.teixeira.subtitles.project.ProjectManager;
import com.teixeira.subtitles.tasks.TaskExecutor;
import com.teixeira.subtitles.ui.ExportWindow;
import com.teixeira.subtitles.utils.DialogUtils;
import com.teixeira.subtitles.utils.FileUtil;
import com.teixeira.subtitles.utils.ToastUtils;
import com.teixeira.subtitles.utils.UiUtils;
import com.teixeira.subtitles.utils.VideoUtils;
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel;
import com.teixeira.subtitles.viewmodels.VideoViewModel;
import java.util.List;

@SuppressWarnings("unused")
public class ProjectActivity extends BaseActivity implements SubtitleListAdapter.SubtitleListener {

  public static final String KEY_PROJECT = "project";
  public static final String KEY_UNDO_MANAGER = "undo_manager";

  private static final Handler mainHandler = ThreadUtils.getMainHandler();

  private ActivityProjectBinding binding;
  private ProjectManager projectManager;
  private Project project;

  private VideoViewModel videoViewModel;
  private SubtitlesViewModel subtitlesViewModel;
  private SubtitleListAdapter subtitleListAdapter;
  private ProgressTracker progressTracker;
  private Runnable saveProjectCallback;

  private ActivityResultLauncher<String[]> subtitleDocumentPicker;
  private ExportWindow exportWindow;

  private boolean isDestroying = false;

  @Override
  protected View bindView() {
    binding = ActivityProjectBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle extras = getIntent().getExtras();
    projectManager = ProjectManager.getInstance();
    project =
        projectManager.setupProject(BundleCompat.getParcelable(extras, KEY_PROJECT, Project.class));
    setSupportActionBar(binding.toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(project.getName());
    getSupportActionBar().setSubtitle(FileUtils.getFileName(project.getVideoPath()));
    binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
    subtitlesViewModel = new ViewModelProvider(this).get(SubtitlesViewModel.class);
    subtitleListAdapter = new SubtitleListAdapter(this);
    subtitleDocumentPicker =
        registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), this::onPickSubtitleFile);
    exportWindow = new ExportWindow(this, subtitlesViewModel);
    progressTracker = new ProgressTracker();
    saveProjectCallback = this::saveProjectAsync;

    if (savedInstanceState != null) {
      subtitlesViewModel.setUndoManager(
          BundleCompat.getParcelable(savedInstanceState, KEY_UNDO_MANAGER, UndoManager.class));
    }

    if (!Preferences.isDevelopmentUndoAndRedoEnabled()) {
      binding.videoControllerContent.redo.setVisibility(View.GONE);
      binding.videoControllerContent.undo.setVisibility(View.GONE);
    }
    subtitlesViewModel.setUndoManagerEnabled(Preferences.isDevelopmentUndoAndRedoEnabled());
    setVideoViewModelObservers();
    setSubtitlesViewModelObservers();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    MaterialAlertDialogBuilder builder =
        DialogUtils.createProgressDialog(this, getString(R.string.proj_loading), false);
    AlertDialog dialog = builder.show();

    TaskExecutor.executeAsyncProvideError(
        () -> project.getSubtitles(),
        (result, throwable) -> {
          dialog.dismiss();
          if (isDestroying) {
            return;
          }
          if (throwable != null) {
            DialogUtils.createSimpleDialog(
                    this, getString(R.string.error_loading_project), throwable.toString())
                .setPositiveButton(R.string.proj_close, (d, w) -> finish())
                .setCancelable(false)
                .show();
            return;
          }
          binding.videoContent.videoView.setVideoPath(project.getVideoPath());
          binding.videoContent.videoView.seekTo(videoViewModel.getCurrentPosition());
          subtitlesViewModel.pushStackToUndoManager(result);
          subtitlesViewModel.setSubtitles(result, false);
          binding.subtitles.setLayoutManager(new LinearLayoutManager(this));
          binding.subtitles.setAdapter(subtitleListAdapter);

          ItemTouchHelper touchHelper =
              new ItemTouchHelper(
                  new SubtitleListAdapter.SubtitleTouchHelper(
                      subtitlesViewModel, subtitleListAdapter));
          touchHelper.attachToRecyclerView(binding.subtitles);

          subtitleListAdapter.setTouchHelper(touchHelper);
          setListeners();
        });
  }

  @Override
  protected void onSaveInstanceState(Bundle outputState) {
    super.onSaveInstanceState(outputState);

    outputState.putParcelable(KEY_UNDO_MANAGER, subtitlesViewModel.getUndoManager());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_project_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == R.id.menu_export) {
      if (subtitlesViewModel.getSubtitles().isEmpty()) {
        ToastUtils.showShort(R.string.error_no_subtitles_to_export);
        return false;
      }
      videoViewModel.pauseVideo();
      exportWindow.showAsDropDown(binding.getRoot(), Gravity.CENTER, 0, 0);
    } else if (item.getItemId() == R.id.menu_import) {
      subtitleDocumentPicker.launch(new String[] {"*/*"});
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    isDestroying = true;
    videoViewModel.setPrepared(false);
    binding.videoContent.videoView.release();
    projectManager.destroy();
    exportWindow.destroy();
    super.onDestroy();

    mainHandler.removeCallbacks(progressTracker, saveProjectCallback);
    progressTracker = null;
    saveProjectCallback = null;
    binding = null;
  }

  @Override
  protected void onPause() {
    videoViewModel.pauseVideo();
    super.onPause();
  }

  @Override
  public void onSubtitleClickListener(View view, int index, Subtitle subtitle) {
    videoViewModel.pauseVideo();
    SubtitleEditorSheetFragment.newInstance(videoViewModel.getCurrentPosition(), index, subtitle)
        .show(getSupportFragmentManager(), null);
  }

  @Override
  public boolean onSubtitleLongClickListener(View view, int index, Subtitle subtitle) {

    return true;
  }

  private void setVideoViewModelObservers() {
    videoViewModel.observeIsPrepared(
        this,
        isPrepared -> {
          UiUtils.setImageEnabled(binding.videoControllerContent.skipBackward, isPrepared);
          UiUtils.setImageEnabled(binding.videoControllerContent.play, isPrepared);
          UiUtils.setImageEnabled(binding.videoControllerContent.skipFoward, isPrepared);
          UiUtils.setImageEnabled(binding.videoControllerContent.addSubtitle, isPrepared);
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
          if (isDestroying) {
            return;
          }
          binding.videoControllerContent.play.setImageResource(
              isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
          binding.videoContent.videoView.setPlaying(isPlaying);
          subtitleListAdapter.setVideoPlaying(isPlaying);
        });
  }

  private void setSubtitlesViewModelObservers() {
    subtitlesViewModel.observeSubtitles(
        this,
        subtitles -> {
          if (subtitles == null) {
            return;
          }
          binding.noSubtitles.setVisibility(subtitles.isEmpty() ? View.VISIBLE : View.GONE);
          subtitleListAdapter.setSubtitles(subtitles);
          binding.timeLine.setSubtitles(subtitles);
          updateVideoUI(videoViewModel.getCurrentPosition());
        });

    subtitlesViewModel.observeUpdateUndoButtons(
        this,
        unused -> {
          UndoManager undoManager = subtitlesViewModel.getUndoManager();
          UiUtils.setImageEnabled(binding.videoControllerContent.redo, undoManager.canRedo());
          UiUtils.setImageEnabled(binding.videoControllerContent.undo, undoManager.canUndo());
        });

    subtitlesViewModel.observeVideoSubtitleIndex(
        this, index -> subtitleListAdapter.setVideoSubtitleIndex(index));

    subtitlesViewModel.observeScrollTo(
        this, position -> binding.subtitles.scrollToPosition(position));

    subtitlesViewModel.observeSaveSubtitles(
        this,
        unused -> {
          if (saveProjectCallback != null) {
            mainHandler.removeCallbacks(saveProjectCallback);
            mainHandler.postDelayed(saveProjectCallback, 10L);
          }
        });
  }

  private void setListeners() {
    binding.videoContent.videoView.setPlayerListener(
        new Player.Listener() {
          @Override
          public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_READY) {
              onVideoPrepared(binding.videoContent.videoView.getPlayer());
            } else if (state == Player.STATE_ENDED) {
              binding.videoControllerContent.play.setImageResource(R.drawable.ic_play);
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
              mainHandler.post(progressTracker);
            }
          }
        });

    binding.videoControllerContent.seekBar.setOnSeekBarChangeListener(
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

    binding.videoControllerContent.play.setOnClickListener(
        v -> videoViewModel.setPlaying(!videoViewModel.isPlaying()));

    binding.videoControllerContent.undo.setOnClickListener(v -> subtitlesViewModel.undo());
    binding.videoControllerContent.redo.setOnClickListener(v -> subtitlesViewModel.redo());
    binding.videoControllerContent.skipBackward.setOnClickListener(
        v -> binding.videoContent.videoView.seekBackward());
    binding.videoControllerContent.skipFoward.setOnClickListener(
        v -> binding.videoContent.videoView.seekFoward());
    binding.videoControllerContent.addSubtitle.setOnClickListener(
        v -> {
          videoViewModel.pauseVideo();
          SubtitleEditorSheetFragment.newInstance(videoViewModel.getCurrentPosition())
              .show(getSupportFragmentManager(), null);
        });
  }

  private void onVideoPrepared(Player player) {
    long duration = player.getDuration();

    binding.videoControllerContent.videoDuration.setText(VideoUtils.getTime(duration));
    binding.videoControllerContent.seekBar.setMax((int) duration);
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

  private void updateVideoUI(long currentPosition) {
    binding.videoControllerContent.currentVideoPosition.setText(
        VideoUtils.getTime(currentPosition));
    binding.videoControllerContent.seekBar.setProgress((int) currentPosition);
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

  private void onPickSubtitleFile(Uri uri) {
    if (uri == null) return;
    MaterialAlertDialogBuilder builder =
        DialogUtils.createSimpleDialog(
            this,
            getString(R.string.proj_import_subtitles),
            getString(R.string.msg_import_subtitles_warning));

    builder.setNegativeButton(R.string.cancel, null);
    builder.setPositiveButton(
        R.string.ok,
        (d, w) -> {
          MaterialAlertDialogBuilder progressBuilder =
              DialogUtils.createProgressDialog(this, getString(R.string.proj_loading), false);
          AlertDialog dialog = progressBuilder.show();

          TaskExecutor.executeAsyncProvideError(
              () -> project.getSubtitleFormat().toList(FileUtil.readFileContent(uri)),
              (result, throwable) -> {
                dialog.dismiss();
                if (isDestroying) {
                  return;
                }
                if (throwable != null) {
                  DialogUtils.createSimpleDialog(
                          this, getString(R.string.error_reading_subtitles), throwable.toString())
                      .setPositiveButton(R.string.ok, null)
                      .show();
                  return;
                }
                subtitlesViewModel.pushStackToUndoManager(result);
                subtitlesViewModel.setSubtitles(result, true);
              });
        });
    builder.show();
  }

  private void saveProjectAsync() {
    getSupportActionBar().setSubtitle(R.string.proj_saving);
    TaskExecutor.executeAsyncProvideError(
        () ->
            FileIOUtils.writeFileFromString(
                project.getProjectPath() + "/" + project.getSubtitleFile().getNameWithExtension(),
                project.getSubtitleFormat().toText(subtitlesViewModel.getSubtitles())),
        (r, throwable) -> {
          getSupportActionBar().setSubtitle(FileUtils.getFileName(project.getVideoPath()));
          if (throwable != null) {
            DialogUtils.createSimpleDialog(
                    this, getString(R.string.error_saving_project), throwable.toString())
                .setPositiveButton(R.string.ok, null)
                .show();
          }
        });
  }

  class ProgressTracker implements Runnable {

    @Override
    public void run() {

      if (isDestroying) {
        return;
      }

      videoViewModel.setCurrentPosition(binding.videoContent.videoView.getCurrentPosition(), false);

      if (videoViewModel.isPlaying() && progressTracker != null) {
        mainHandler.post(progressTracker);
      }
    }
  }
}
