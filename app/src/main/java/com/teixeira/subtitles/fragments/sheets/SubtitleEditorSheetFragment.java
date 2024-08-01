package com.teixeira.subtitles.fragments.sheets;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.os.BundleCompat;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.adapters.SubtitleListAdapter;
import com.teixeira.subtitles.callbacks.GetSubtitleListAdapterCallback;
import com.teixeira.subtitles.databinding.FragmentSubtitleEditorBinding;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.utils.OnTextChangedListener;
import com.teixeira.subtitles.utils.VideoUtils;
import java.util.List;

public class SubtitleEditorSheetFragment extends BottomSheetDialogFragment {

  private static final long UPDATE_PREVIEW_DELAY = 100L;
  private static final Handler mainHandler = ThreadUtils.getMainHandler();

  private FragmentSubtitleEditorBinding binding;
  private SubtitleListAdapter adapter;
  private Subtitle subtitle;
  private int index = -1;
  private long currentVideoPosition;

  private Runnable updatePreviewCallback;

  public static SubtitleEditorSheetFragment newInstance(long currentVideoPosition) {
    return newInstance(currentVideoPosition, -1, null);
  }

  public static SubtitleEditorSheetFragment newInstance(
      long currentVideoPosition, int index, Subtitle subtitle) {
    SubtitleEditorSheetFragment fragment = new SubtitleEditorSheetFragment();
    Bundle args = new Bundle();
    args.putLong("currentVideoPosition", currentVideoPosition);
    if (subtitle != null) {
      args.putParcelable("subtitle", subtitle);
      args.putInt("index", index);
    }
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    if (context instanceof GetSubtitleListAdapterCallback) {
      var callback = (GetSubtitleListAdapterCallback) context;
      adapter = callback.getSubtitleListAdapter();
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

    currentVideoPosition = args.getLong("currentVideoPosition");

    if (args.containsKey("subtitle") && args.containsKey("index")) {
      subtitle = BundleCompat.getParcelable(args, "subtitle", Subtitle.class).clone();
      index = args.getInt("index");
    } else {
      subtitle =
          new Subtitle(
              VideoUtils.getTime(currentVideoPosition),
              VideoUtils.getTime(currentVideoPosition + 2000),
              "");
      index = -1;

      binding.deleteSubtitle.setVisibility(View.INVISIBLE);
    }

    updatePreviewCallback = () -> binding.preview.setSubtitle(subtitle);

    binding.currentVideoPosition.setText(
        getString(R.string.current_video_position, VideoUtils.getTime(currentVideoPosition)));
    binding.currentVideoPosition.setOnClickListener(
        v -> ClipboardUtils.copyText(VideoUtils.getTime(currentVideoPosition)));
    binding.deleteSubtitle.setOnClickListener(v -> showAlertToDeleteSubtitle());

    binding.tieStartTime.setText(subtitle.getStartTime());
    binding.tieEndTime.setText(subtitle.getEndTime());
    binding.tieText.setText(subtitle.getText());
    binding.preview.setSubtitle(subtitle);
    binding.dialogButtons.cancel.setOnClickListener(v -> dismiss());
    binding.dialogButtons.save.setOnClickListener(v -> saveSubtitle());
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
            String time = editable.toString();
            if (validateSubtitleTime(time)) {
              subtitle.setStartTime(time);
            }
          }
        });
    binding.tieEndTime.addTextChangedListener(
        new OnTextChangedListener() {

          @Override
          public void afterTextChanged(Editable editable) {
            String time = editable.toString();
            if (validateSubtitleTime(time)) {
              subtitle.setEndTime(time);
            }
          }
        });
    binding.tieText.addTextChangedListener(
        new OnTextChangedListener() {

          @Override
          public void afterTextChanged(Editable editable) {
            subtitle.setText(editable.toString().trim());
            updatePreview();
          }
        });
  }

  private boolean validateSubtitleTime(String time) {
    boolean isValidTime = VideoUtils.isValidTime(time.split(":"));
    binding.dialogButtons.save.setEnabled(isValidTime);
    return isValidTime;
  }

  private void showAlertToDeleteSubtitle() {
    new MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.delete)
        .setMessage(getString(R.string.delete_message, subtitle.getText()))
        .setPositiveButton(
            R.string.yes,
            (d, w) -> {
              dismiss();
              adapter.removeSubtitle(index);
            })
        .setNegativeButton(R.string.no, null)
        .show();
  }

  private void saveSubtitle() {
    dismiss();
    if (index != -1) {
      adapter.setSubtitle(index, subtitle);
    } else {
      adapter.addSubtitle(getIndexForNewSubtitle(), subtitle);
    }
  }

  private int getIndexForNewSubtitle() {
    List<Subtitle> subtitles = adapter.getSubtitles();
    int index = subtitles.size();
    for (int i = 0; i < subtitles.size(); i++) {
      Subtitle sub = subtitles.get(i);

      long subStartTime = VideoUtils.getMilliSeconds(sub.getStartTime());

      if (subStartTime >= currentVideoPosition) {
        index = i;
        break;
      }
    }
    return index;
  }

  private void updatePreview() {

    if (updatePreviewCallback == null) {
      return;
    }

    mainHandler.removeCallbacks(updatePreviewCallback);
    mainHandler.postDelayed(updatePreviewCallback, UPDATE_PREVIEW_DELAY);
  }
}
