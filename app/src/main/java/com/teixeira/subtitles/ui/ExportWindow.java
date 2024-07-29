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
import com.google.android.material.color.MaterialColors;
import com.teixeira.subtitles.adapters.ExportTypeListAdapter;
import com.teixeira.subtitles.adapters.SubtitleListAdapter;
import com.teixeira.subtitles.databinding.LayoutExportWindowBinding;
import com.teixeira.subtitles.source.SubtitleSourceMaker;
import com.teixeira.subtitles.utils.FileUtil;

public class ExportWindow extends PopupWindow {

  private LayoutExportWindowBinding binding;
  private ExportTypeListAdapter exportTypeListAdapter;
  private SubtitleListAdapter subtitleListAdapter;
  private SubtitleSourceMaker subtitleSourceMaker;
  private ActivityResultLauncher<String> subtitleFileSaver;

  public ExportWindow(AppCompatActivity activity, SubtitleListAdapter subtitleListAdapter) {
    super(activity);

    this.subtitleListAdapter = subtitleListAdapter;
    binding = LayoutExportWindowBinding.inflate(activity.getLayoutInflater());
    subtitleSourceMaker = new SubtitleSourceMaker();
    subtitleFileSaver =
        activity.registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/srt"), this::onSaveSubtitleFile);

    binding.close.setOnClickListener(v -> dismiss());
    binding.export.setOnClickListener(this::onExportClick);

    exportTypeListAdapter = new ExportTypeListAdapter(activity);
    binding.exportTypes.setLayoutManager(new LinearLayoutManager(activity));
    binding.exportTypes.setAdapter(exportTypeListAdapter);

    setWidth(WindowManager.LayoutParams.MATCH_PARENT);
    setHeight(WindowManager.LayoutParams.MATCH_PARENT);
    setContentView(binding.getRoot());
    setBackgroundDrawable(createBackground());
    setFocusable(true);
  }

  public void destroy() {
    subtitleFileSaver.unregister();
    binding = null;
  }

  private void onExportClick(View view) {
    subtitleFileSaver.launch("subtitle.srt");
  }

  private void onSaveSubtitleFile(Uri uri) {
    if (uri != null) {
      FileUtil.writeFileContent(
          uri, subtitleSourceMaker.makeSubRipSource(subtitleListAdapter.getSubtitles()));

      dismiss();
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
