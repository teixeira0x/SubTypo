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
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.ObjectsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.adapters.SubtitleListAdapter;
import com.teixeira.subtitles.fragments.sheets.SubtitleEditorSheetFragment;
import com.teixeira.subtitles.managers.UndoManager;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.subtitle.file.SubtitleFile;
import com.teixeira.subtitles.subtitle.format.SubtitleFormat;
import com.teixeira.subtitles.tasks.TaskExecutor;
import com.teixeira.subtitles.utils.DialogUtils;
import com.teixeira.subtitles.utils.FileUtil;
import com.teixeira.subtitles.utils.UiUtils;

/**
 * Base class for ProjectActivity that handles most things related to the caption list.
 *
 * @author FelipeTeixeira
 */
abstract class SubtitleListHandlerActivity extends VideoHandlerActivity
    implements SubtitleListAdapter.SubtitleListener {

  private SubtitleListAdapter subtitleListAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    subtitleListAdapter = new SubtitleListAdapter(this);

    binding.subtitles.setLayoutManager(new LinearLayoutManager(this));
    binding.subtitles.setAdapter(subtitleListAdapter);

    ItemTouchHelper touchHelper =
        new ItemTouchHelper(
            new SubtitleListAdapter.SubtitleTouchHelper(subtitlesViewModel, subtitleListAdapter));
    touchHelper.attachToRecyclerView(binding.subtitles);

    subtitleListAdapter.setTouchHelper(touchHelper);

    setSubtitlesViewModelObservers();
  }

  @NonNull
  @Override
  protected SubtitleListAdapter requireSubtitleListAdapter() {
    return ObjectsCompat.requireNonNull(subtitleListAdapter);
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

  @Override
  protected void onPickSubtitleFile(@Nullable Uri uri) {
    super.onPickSubtitleFile(uri);

    if (uri != null) {
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
                () ->
                    projectViewModel
                        .getSelectedSubtitleFile()
                        .getSubtitleFormat()
                        .toList(FileUtil.readFileContent(uri)),
                (result, throwable) -> {
                  dialog.dismiss();
                  if (isDestroying()) {
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
  }

  @Override
  protected void onSelectSubtitleFile(SubtitleFile subtitleFile) {
    super.onSelectSubtitleFile(subtitleFile);

    SubtitleFormat subtitleFormat = subtitleFile.getSubtitleFormat();

    MaterialAlertDialogBuilder builder =
        DialogUtils.createProgressDialog(this, getString(R.string.proj_loading), false);
    AlertDialog dialog = builder.show();

    TaskExecutor.executeAsyncProvideError(
        () -> subtitleFormat.toList(subtitleFile.getContent()),
        (result, throwable) -> {
          dialog.dismiss();
          if (isDestroying()) {
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
          subtitlesViewModel.setSubtitles(result, false);
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
          UiUtils.setImageEnabled(binding.controllerContent.redo, undoManager.canRedo());
          UiUtils.setImageEnabled(binding.controllerContent.undo, undoManager.canUndo());
        });

    subtitlesViewModel.observeVideoSubtitleIndex(
        this, index -> subtitleListAdapter.setVideoSubtitleIndex(index));

    subtitlesViewModel.observeScrollTo(
        this, position -> binding.subtitles.scrollToPosition(position));

    subtitlesViewModel.observeSaveSubtitles(this, unused -> saveProject());
  }
}
