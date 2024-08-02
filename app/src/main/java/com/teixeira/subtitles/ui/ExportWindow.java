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

package com.teixeira.subtitles.ui;

import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blankj.utilcode.util.UriUtils;
import com.google.android.material.color.MaterialColors;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.adapters.ExportFormatListAdapter;
import com.teixeira.subtitles.adapters.SubtitleListAdapter;
import com.teixeira.subtitles.databinding.LayoutExportWindowBinding;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.project.ProjectManager;
import com.teixeira.subtitles.subtitle.file.SubtitleFile;
import com.teixeira.subtitles.utils.DialogUtils;
import com.teixeira.subtitles.utils.FileUtil;
import com.teixeira.subtitles.utils.ToastUtils;

public class ExportWindow extends PopupWindow {

  private LayoutExportWindowBinding binding;
  private ExportFormatListAdapter exportFormatListAdapter;
  private SubtitleListAdapter subtitleListAdapter;
  private ActivityResultLauncher<String> subtitleFileSaver;
  private SubtitleFile subtitleFile;

  public ExportWindow(AppCompatActivity activity) {
    super(activity);

    binding = LayoutExportWindowBinding.inflate(activity.getLayoutInflater());

    subtitleFileSaver =
        activity.registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/srt"), this::onSaveSubtitleFile);

    binding.close.setOnClickListener(v -> dismiss());
    binding.export.setOnClickListener(this::onExportClick);

    Project project = ProjectManager.getInstance().getProject();
    this.subtitleFile = project.getSubtitleFile();

    exportFormatListAdapter = new ExportFormatListAdapter(activity);
    binding.exportFormats.setLayoutManager(new LinearLayoutManager(activity));
    binding.exportFormats.setAdapter(exportFormatListAdapter);

    setWidth(WindowManager.LayoutParams.MATCH_PARENT);
    setHeight(WindowManager.LayoutParams.MATCH_PARENT);
    setContentView(binding.getRoot());
    setBackgroundDrawable(createBackground());
    setFocusable(true);
  }

  public void setSubtitleListAdapter(SubtitleListAdapter subtitleListAdapter) {
    this.subtitleListAdapter = subtitleListAdapter;
  }

  public void destroy() {
    subtitleFileSaver.unregister();
    binding = null;
  }

  private void onExportClick(View view) {
    subtitleFileSaver.launch(subtitleFile.getNameWithExtension());
  }

  private void onSaveSubtitleFile(Uri uri) {
    dismiss();
    if (uri != null && subtitleListAdapter != null) {
      try {
        FileUtil.writeFileContent(
            uri, subtitleFile.getSubtitleFormat().toText(subtitleListAdapter.getSubtitles()));
        ToastUtils.showLong(
            R.string.proj_export_saved, UriUtils.uri2FileNoCacheCopy(uri).getAbsolutePath());
      } catch (Exception e) {
        DialogUtils.createSimpleDialog(
                getContentView().getContext(),
                getContentView().getContext().getString(R.string.error_exporting_subtitles),
                e.toString())
            .setPositiveButton(R.string.ok, null)
            .show();
      }
    }
  }

  private GradientDrawable createBackground() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setStroke(
        2,
        MaterialColors.getColor(
            getContentView(), com.google.android.material.R.attr.colorOutlineVariant));
    gradientDrawable.setColor(
        MaterialColors.getColor(getContentView(), com.google.android.material.R.attr.colorSurface));
    gradientDrawable.setCornerRadius(15);
    return gradientDrawable;
  }
}
