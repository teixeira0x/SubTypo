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
import android.media.MediaPlayer;
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
import com.teixeira.subtitles.utils.VideoUtils;
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel;
import com.teixeira.subtitles.viewmodels.VideoViewModel;
import java.util.List;

public class ProjectActivity extends BaseActivity implements SubtitleListAdapter.SubtitleListener {

  public static final String KEY_PROJECT = "project";

  private static final Handler mainHandler = ThreadUtils.getMainHandler();

  private ActivityProjectBinding binding;
  private ProjectManager projectManager;
  private Project project;

  private VideoViewModel videoViewModel;
  private SubtitlesViewModel subtitlesViewModel;
  private SubtitleListAdapter subtitleListAdapter;
  private Runnable saveProjectCallback;

  private ActivityResultLauncher<String[]> subtitleDocumentPicker;
  private ExportWindow exportWindow;

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
    saveProjectCallback = this::saveProjectAsync;

    if (!Preferences.isDevelopmentUndoAndRedoEnabled()) {
      binding.videoControllerContent.redo.setVisibility(View.GONE);
      binding.videoControllerContent.undo.setVisibility(View.GONE);
    }
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
        () -> {
          setListeners();

          return project.getSubtitles();
        },
        (result, throwable) -> {
          dialog.dismiss();
          if (throwable != null) {
            DialogUtils.createSimpleDialog(
                    this, getString(R.string.error_loading_project), throwable.toString())
                .setPositiveButton(R.string.proj_close, (d, w) -> finish())
                .setCancelable(false)
                .show();
            return;
          }
          binding.videoContent.videoView.setVideoPath(project.getVideoPath());
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
        });
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
    super.onDestroy();

    mainHandler.removeCallbacks(saveProjectCallback);
    saveProjectCallback = null;

    projectManager.destroy();
    exportWindow.destroy();
    binding = null;
  }

  @Override
  protected void onPause() {
    videoViewModel.setCurrentVideoPosition(
        binding.videoContent.videoView.getCurrentPosition(), false);
    videoViewModel.pauseVideo();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();

    videoViewModel.setCurrentVideoPosition(videoViewModel.getCurrentVideoPosition(), true);
  }

  @Override
  public void onSubtitleClickListener(View view, int index, Subtitle subtitle) {
    videoViewModel.pauseVideo();
    SubtitleEditorSheetFragment.newInstance(
            binding.videoContent.videoView.getCurrentPosition(), index, subtitle)
        .show(getSupportFragmentManager(), null);
  }

  @Override
  public boolean onSubtitleLongClickListener(View view, int index, Subtitle subtitle) {

    return true;
  }

  private void setVideoViewModelObservers() {
    videoViewModel.observeCurrentVideoPosition(
        this,
        currentVideoPositionPair -> {
          int currentVideoPosition = currentVideoPositionPair.first;
          boolean seekTo = currentVideoPositionPair.second;
          if (seekTo) {
            binding.videoContent.videoView.seekTo(currentVideoPosition);
          }
          updateVideoUI(currentVideoPosition);
        });

    videoViewModel.observeIsVideoPlaying(
        this,
        isVideoPlaying -> {
          if (isVideoPlaying) {
            binding.videoControllerContent.play.setImageResource(R.drawable.ic_pause);
            binding.videoContent.videoView.start();
          } else {
            binding.videoControllerContent.play.setImageResource(R.drawable.ic_play);
            binding.videoContent.videoView.pause();
          }
          subtitleListAdapter.setVideoPlaying(isVideoPlaying);
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
          updateVideoUI(videoViewModel.getCurrentVideoPosition());
        });

    subtitlesViewModel.observeUpdateUndoButtons(
        this,
        unused -> {
          UndoManager undoManager = subtitlesViewModel.getUndoManager();
          binding
              .videoControllerContent
              .redo
              .animate()
              .alpha(undoManager.canRedo() ? 1.0f : 0.5f)
              .start();
          binding.videoControllerContent.redo.setClickable(undoManager.canRedo());
          binding
              .videoControllerContent
              .undo
              .animate()
              .alpha(undoManager.canUndo() ? 1.0f : 0.5f)
              .start();
          binding.videoControllerContent.undo.setClickable(undoManager.canUndo());
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
    binding.videoContent.videoView.setOnPreparedListener(this::onVideoPrepared);
    binding.videoContent.videoView.setOnCompletionListener(
        player -> binding.videoControllerContent.play.setImageResource(R.drawable.ic_play));

    binding.videoContent.videoView.setOnEveryMilliSecondListener(
        currentVideoPosition ->
            videoViewModel.setCurrentVideoPosition(currentVideoPosition, false));

    binding.videoControllerContent.seekBar.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {

          private boolean wasPlaying;

          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
              videoViewModel.setCurrentVideoPosition(progress, true);
            }
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
            wasPlaying = videoViewModel.isVideoPlaying();
            if (wasPlaying) videoViewModel.pauseVideo();
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
            if (wasPlaying) videoViewModel.playVideo();
          }
        });

    binding.videoControllerContent.play.setOnClickListener(
        v -> videoViewModel.setVideoPlaying(!videoViewModel.isVideoPlaying()));

    binding.videoControllerContent.undo.setOnClickListener(v -> subtitlesViewModel.undo());
    binding.videoControllerContent.redo.setOnClickListener(v -> subtitlesViewModel.redo());
    binding.videoControllerContent.skipBackward.setOnClickListener(v -> videoViewModel.back5sec());
    binding.videoControllerContent.skipFoward.setOnClickListener(v -> videoViewModel.skip5sec());
    binding.videoControllerContent.addSubtitle.setOnClickListener(
        v -> {
          videoViewModel.pauseVideo();
          SubtitleEditorSheetFragment.newInstance(
                  binding.videoContent.videoView.getCurrentPosition())
              .show(getSupportFragmentManager(), null);
        });
  }

  private void onVideoPrepared(MediaPlayer player) {
    int videoDuration = player.getDuration();
    binding.videoControllerContent.videoDuration.setText(VideoUtils.getTime(videoDuration));
    binding.timeLine.setVideoDuration(videoDuration);
    binding.videoControllerContent.seekBar.setMax(videoDuration);
    videoViewModel.setVideoDuration(videoDuration);

    int width = player.getVideoWidth();
    int height = player.getVideoHeight();

    if (width > height) {
      binding.videoContent.videoView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
      binding.videoContent.videoView.requestLayout();
    } else if (height > width
        && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
      binding.videoContent.getRoot().getLayoutParams().height = SizeUtils.dp2px(350f);
      binding.videoContent.getRoot().requestLayout();
    }
    updateVideoUI(videoViewModel.getCurrentVideoPosition());
  }

  private void updateVideoUI(int currentVideoPosition) {
    binding.videoControllerContent.currentVideoPosition.setText(
        VideoUtils.getTime(currentVideoPosition));
    binding.videoControllerContent.seekBar.setProgress(currentVideoPosition);
    binding.timeLine.setCurrentVideoPosition(currentVideoPosition);

    List<Subtitle> subtitles = subtitlesViewModel.getSubtitles();
    boolean subtitleFound = false;
    for (int i = 0; i < subtitles.size(); i++) {
      Subtitle subtitle = subtitles.get(i);
      long startTime = VideoUtils.getMilliSeconds(subtitle.getStartTime());
      long endTime = VideoUtils.getMilliSeconds(subtitle.getEndTime());

      if (currentVideoPosition >= startTime && currentVideoPosition <= endTime) {
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
}
