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

import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.ThreadUtils;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.activities.BaseActivity;
import com.teixeira.subtitles.adapters.SubtitleListAdapter;
import com.teixeira.subtitles.databinding.ActivityProjectBinding;
import com.teixeira.subtitles.managers.UndoManager;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.project.ProjectManager;
import com.teixeira.subtitles.project.ProjectRepository;
import com.teixeira.subtitles.subtitle.file.SubtitleFile;
import com.teixeira.subtitles.tasks.TaskExecutor;
import com.teixeira.subtitles.ui.ExportWindow;
import com.teixeira.subtitles.utils.DialogUtils;
import com.teixeira.subtitles.utils.FileUtil;
import com.teixeira.subtitles.utils.ToastUtils;
import com.teixeira.subtitles.viewmodels.ProjectViewModel;
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel;
import com.teixeira.subtitles.viewmodels.VideoViewModel;

/**
 * Base class for ProjectActivity that handles most project related things.
 *
 * @author Felipe Teixeira
 */
abstract class BaseProjectActivity extends BaseActivity {

  public static final String KEY_PROJECT = "project";
  public static final String KEY_UNDO_MANAGER = "undo_manager";

  protected ActivityProjectBinding binding;
  protected ProjectManager projectManager;
  protected Project project;

  protected ProjectViewModel projectViewModel;
  protected VideoViewModel videoViewModel;
  protected SubtitlesViewModel subtitlesViewModel;

  private Runnable saveProjectCallback;
  private boolean isDestroying = false;

  private ActivityResultLauncher<String[]> subtitleFilePicker;
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
    if (!extras.containsKey(KEY_PROJECT)) {
      throw new IllegalArgumentException("You cannot open this activity without a project.");
    }

    projectManager = ProjectManager.getInstance();
    project = BundleCompat.getParcelable(extras, KEY_PROJECT, Project.class);
    projectManager.openProject(project);

    saveProjectCallback = this::saveProjectAsync;

    projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
    videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
    subtitlesViewModel = new ViewModelProvider(this).get(SubtitlesViewModel.class);
    subtitleFilePicker =
        registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), this::onPickSubtitleFile);
    exportWindow = new ExportWindow(this, subtitlesViewModel);

    if (savedInstanceState != null) {
      subtitlesViewModel.setUndoManager(
          BundleCompat.getParcelable(savedInstanceState, KEY_UNDO_MANAGER, UndoManager.class));
    }

    setSupportActionBar(binding.toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(project.getName());
    getSupportActionBar().setSubtitle(FileUtil.getFileName(project.getVideoPath()));
    binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    observeProjectViewModel();
    configureListeners();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    projectViewModel.setSelectedSubtitleFile(projectManager.getSubtitleFiles().get(0));
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
      subtitleFilePicker.launch(new String[] {"*/*"});
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    isDestroying = true;
    preDestroy();
    super.onDestroy();
    postDestroy();
  }

  protected void preDestroy() {
    projectManager.destroy();
    exportWindow.destroy();
  }

  protected void postDestroy() {
    ThreadUtils.getMainHandler().removeCallbacks(saveProjectCallback);
    saveProjectCallback = null;
    binding = null;
  }

  protected boolean isDestroying() {
    return isDestroying;
  }

  protected void configureListeners() {}

  protected void onPickSubtitleFile(@Nullable Uri uri) {}

  protected void onSelectSubtitleFile(SubtitleFile subtitleFile) {
    exportWindow.setSubtitleFile(subtitleFile);
  }

  @NonNull
  protected abstract SubtitleListAdapter requireSubtitleListAdapter();

  protected void saveProject() {
    if (saveProjectCallback != null) {
      ThreadUtils.getMainHandler().removeCallbacks(saveProjectCallback);
      ThreadUtils.getMainHandler().postDelayed(saveProjectCallback, 10L);
    }
  }

  private void observeProjectViewModel() {
    projectViewModel.observeSelectedSubtitleFile(
        this,
        subtitleFile -> {
          if (subtitleFile != null) onSelectSubtitleFile(subtitleFile);
        });
  }

  private void saveProjectAsync() {
    getSupportActionBar().setSubtitle(R.string.proj_saving);
    TaskExecutor.executeAsyncProvideError(
        () -> {
          SubtitleFile subtitleFile = projectViewModel.getSelectedSubtitleFile();
          subtitleFile.setContent(
              subtitleFile.getSubtitleFormat().toText(subtitlesViewModel.getSubtitles()));

          ProjectRepository.writeSubtitleDataFile(project, projectManager.getSubtitleFiles());
          return null;
        },
        (r, throwable) -> {
          getSupportActionBar().setSubtitle(FileUtil.getFileName(project.getVideoPath()));
          if (throwable != null) {
            DialogUtils.createSimpleDialog(
                    this, getString(R.string.error_saving_project), throwable.toString())
                .setPositiveButton(R.string.ok, null)
                .show();
          }
        });
  }
}
