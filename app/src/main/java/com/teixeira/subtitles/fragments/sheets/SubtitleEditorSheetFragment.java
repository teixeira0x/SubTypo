package com.teixeira.subtitles.fragments.sheets;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.teixeira.subtitles.callbacks.SubtitleEditorCallbacks;
import com.teixeira.subtitles.databinding.FragmentSubtitleEditorBinding;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.utils.OnTextChangedListener;
import com.teixeira.subtitles.utils.VideoUtils;

public class SubtitleEditorSheetFragment extends BottomSheetDialogFragment {

  private static final long UPDATE_PREVIEW_DELAY = 100L;
  private static final Handler mainHandler = ThreadUtils.getMainHandler();

  private FragmentSubtitleEditorBinding binding;
  private SubtitleEditorCallbacks callbacks;
  private Subtitle subtitle;
  private int index = -1;
  private long currentVideoTime;

  private Runnable updatePreviewCallback;

  public static SubtitleEditorSheetFragment newInstance(long currentVideoTime) {
    return newInstance(currentVideoTime, -1, null);
  }

  public static SubtitleEditorSheetFragment newInstance(
      long currentVideoTime, int index, Subtitle subtitle) {
    SubtitleEditorSheetFragment fragment = new SubtitleEditorSheetFragment();
    Bundle args = new Bundle();
    args.putLong("currentVideoTime", currentVideoTime);
    if (subtitle != null) {
      args.putParcelable("subtitle", subtitle);
      args.putInt("index", index);
    }
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    if (context instanceof SubtitleEditorCallbacks) {
      callbacks = (SubtitleEditorCallbacks) context;
    }
    super.onAttach(context);
  }

  @NonNull
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentSubtitleEditorBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {

    Bundle args = getArguments();

    if (args == null) {
      throw new IllegalArgumentException("Arguments cannot be null");
    }

    currentVideoTime = args.getLong("currentVideoTime");

    if (args.containsKey("subtitle") && args.containsKey("index")) {
      subtitle = args.getParcelable("subtitle", Subtitle.class).clone();
      index = args.getInt("index");
    } else {

      String start = VideoUtils.getTime(currentVideoTime);
      String end = VideoUtils.getTime(currentVideoTime + 2000);

      subtitle = new Subtitle(start, end, "");
      index = -1;

      binding.deleteSubtitle.setVisibility(View.GONE);
    }

    updatePreviewCallback = () -> binding.preview.setSubtitle(subtitle);

    binding.currentVideoTime.setText(VideoUtils.getTime(currentVideoTime));
    binding.currentVideoTime.setOnClickListener(
        v -> ClipboardUtils.copyText(binding.currentVideoTime.getText().toString()));
    binding.deleteSubtitle.setOnClickListener(
        v -> {
          dismiss();
          if (callbacks != null) {
            callbacks.removeSubtitle(index);
          }
        });

    binding.tieStartTime.setText(subtitle.getStartTime());
    binding.tieEndTime.setText(subtitle.getEndTime());
    binding.tieText.setText(subtitle.getText());
    binding.preview.setSubtitle(subtitle);
    binding.cancel.setOnClickListener(v -> dismiss());
    binding.save.setOnClickListener(v -> saveSubtitle());
    configureTextWatchers();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mainHandler.removeCallbacks(updatePreviewCallback);
    updatePreviewCallback = null;
    binding = null;
  }

  private void configureTextWatchers() {
    binding.tieStartTime.addTextChangedListener(
        new OnTextChangedListener() {

          @Override
          public void afterTextChanged(Editable editable) {
            subtitle.setStartTime(editable.toString());
          }
        });
    binding.tieEndTime.addTextChangedListener(
        new OnTextChangedListener() {

          @Override
          public void afterTextChanged(Editable editable) {
            subtitle.setEndTime(editable.toString());
          }
        });
    binding.tieText.addTextChangedListener(
        new OnTextChangedListener() {

          @Override
          public void afterTextChanged(Editable editable) {
            subtitle.setText(editable.toString());
            updatePreview();
          }
        });
  }

  private void saveSubtitle() {
    dismiss();
    if (callbacks != null) {
      if (index != -1) {
        callbacks.updateSubtitle(index, subtitle);
      } else {
        callbacks.addSubtitle(subtitle);
      }
    }
  }

  private void updatePreview() {

    if (updatePreviewCallback == null) {
      return;
    }

    mainHandler.removeCallbacks(updatePreviewCallback);
    mainHandler.postDelayed(updatePreviewCallback, UPDATE_PREVIEW_DELAY);
  }
}
