package com.teixeira.subtitles.fragments.sheets;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.ClipboardUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.databinding.FragmentSubtitleEditorBinding;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.utils.OnTextChangedListener;
import com.teixeira.subtitles.utils.VideoUtils;
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel;
import java.util.List;

public class SubtitleEditorSheetFragment extends DialogFragment {

  public static final String KEY_CURRENT_VIDEO_POSITION = "current_video_position";
  public static final String KEY_SELECTED_SUBTITLE = "selected_subtitle";
  public static final String KEY_SELECTED_SUBTITLE_INDEX = "selected_subtitle_index";

  private FragmentSubtitleEditorBinding binding;
  private SubtitlesViewModel viewModel;

  private Subtitle previewSubtitile;
  private Subtitle editingSubtitle;
  private int index = -1;
  private int currentVideoPosition;

  public static SubtitleEditorSheetFragment newInstance(int currentVideoPosition) {
    return newInstance(currentVideoPosition, -1, null);
  }

  public static SubtitleEditorSheetFragment newInstance(
      int currentVideoPosition, int index, Subtitle editingSubtitle) {
    SubtitleEditorSheetFragment fragment = new SubtitleEditorSheetFragment();
    Bundle args = new Bundle();
    args.putInt(KEY_CURRENT_VIDEO_POSITION, currentVideoPosition);
    if (editingSubtitle != null) {
      args.putParcelable(KEY_SELECTED_SUBTITLE, editingSubtitle);
      args.putInt(KEY_SELECTED_SUBTITLE_INDEX, index);
    }
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(getActivity()).get(SubtitlesViewModel.class);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    BottomSheetDialog sheetDialog = new BottomSheetDialog(requireContext());
    binding = FragmentSubtitleEditorBinding.inflate(sheetDialog.getLayoutInflater());
    sheetDialog.setContentView(binding.getRoot());

    BottomSheetBehavior<FrameLayout> behavior = sheetDialog.getBehavior();
    behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    init(savedInstanceState);
    return sheetDialog;
  }

  public void init(Bundle savedInstanceState) {

    Bundle args = getArguments();

    if (args == null) {
      throw new IllegalArgumentException("Arguments cannot be null");
    }

    currentVideoPosition = args.getInt(KEY_CURRENT_VIDEO_POSITION);

    if (args.containsKey(KEY_SELECTED_SUBTITLE) && args.containsKey(KEY_SELECTED_SUBTITLE_INDEX)) {
      editingSubtitle = BundleCompat.getParcelable(args, KEY_SELECTED_SUBTITLE, Subtitle.class);
      index = args.getInt(KEY_SELECTED_SUBTITLE_INDEX);
    } else {
      editingSubtitle =
          new Subtitle(
              VideoUtils.getTime(currentVideoPosition),
              VideoUtils.getTime(currentVideoPosition + 2000),
              "");
      index = -1;

      binding.deleteSubtitle.setVisibility(View.INVISIBLE);
    }
    previewSubtitile = editingSubtitle.clone();

    binding.currentVideoPosition.setText(
        getString(R.string.proj_current_video_position, VideoUtils.getTime(currentVideoPosition)));
    binding.currentVideoPosition.setOnClickListener(
        v -> ClipboardUtils.copyText(VideoUtils.getTime(currentVideoPosition)));
    binding.deleteSubtitle.setOnClickListener(v -> showAlertToDeleteSubtitle());

    binding.tieStartTime.setText(editingSubtitle.getStartTime());
    binding.tieEndTime.setText(editingSubtitle.getEndTime());
    binding.tieText.setText(editingSubtitle.getText());
    binding.preview.setSubtitle(previewSubtitile);
    binding.dialogButtons.cancel.setOnClickListener(v -> dismiss());
    binding.dialogButtons.save.setOnClickListener(v -> saveSubtitle());
    configureTextWatchers();
    validateFields();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void configureTextWatchers() {

    OnTextChangedListener validateFieldsTextWatcher =
        new OnTextChangedListener() {

          @Override
          public void afterTextChanged(Editable editable) {
            validateFields();
          }
        };

    binding.tieStartTime.addTextChangedListener(validateFieldsTextWatcher);
    binding.tieEndTime.addTextChangedListener(validateFieldsTextWatcher);
    binding.tieText.addTextChangedListener(
        new OnTextChangedListener() {

          @Override
          public void afterTextChanged(Editable editable) {
            previewSubtitile.setText(editable.toString().trim());
            binding.preview.invalidate();
            validateFields();
          }
        });

    binding.tieText.requestFocus();
  }

  private void validateFields() {
    binding.dialogButtons.save.setEnabled(
        isValidSubtitleTime(binding.tieStartTime.getText().toString())
            && isValidSubtitleTime(binding.tieEndTime.getText().toString())
            && isValidSubtitleText(binding.tieText.getText().toString()));
  }

  private boolean isValidSubtitleTime(String time) {
    return VideoUtils.isValidTime(time.split(":"));
  }

  private boolean isValidSubtitleText(String text) {
    if (TextUtils.isEmpty(text)) {
      return false;
    }

    String[] lines = text.trim().split("\n");
    for (String line : lines) {
      if (TextUtils.isEmpty(line)) {
        return false;
      }
    }
    return true;
  }

  private void showAlertToDeleteSubtitle() {
    new MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.delete)
        .setMessage(getString(R.string.msg_delete_confirmation, editingSubtitle.getText()))
        .setPositiveButton(
            R.string.yes,
            (d, w) -> {
              dismiss();
              viewModel.removeSubtitle(index);
            })
        .setNegativeButton(R.string.no, null)
        .show();
  }

  private void saveSubtitle() {
    editingSubtitle.setStartTime(binding.tieStartTime.getText().toString());
    editingSubtitle.setEndTime(binding.tieEndTime.getText().toString());
    editingSubtitle.setText(binding.tieText.getText().toString());

    if (index >= 0) {
      viewModel.setSubtitle(index, editingSubtitle);
    } else {
      viewModel.addSubtitle(getIndexForNewSubtitle(), editingSubtitle);
    }
    dismiss();
  }

  private int getIndexForNewSubtitle() {
    List<Subtitle> subtitles = viewModel.getSubtitles();
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
}
