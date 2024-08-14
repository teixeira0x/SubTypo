package com.teixeira.subtitles.fragments.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.ClipboardUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.databinding.FragmentDialogSubtitleEditorBinding;
import com.teixeira.subtitles.subtitle.models.Subtitle;
import com.teixeira.subtitles.subtitle.models.Time;
import com.teixeira.subtitles.subtitle.utils.TimeUtils;
import com.teixeira.subtitles.utils.OnTextChangedListener;
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel;
import java.util.List;

public class SubtitleEditorDialogFragment extends DialogFragment {

  public static final String KEY_CURRENT_VIDEO_POSITION = "current_video_position";
  public static final String KEY_SELECTED_SUBTITLE = "selected_subtitle";
  public static final String KEY_SELECTED_SUBTITLE_INDEX = "selected_subtitle_index";

  private FragmentDialogSubtitleEditorBinding binding;
  private SubtitlesViewModel viewModel;

  private Subtitle previewSubtitile;
  private Subtitle editingSubtitle;
  private long currentPosition;
  private int index = -1;

  public static SubtitleEditorDialogFragment newInstance(long currentPosition) {
    return newInstance(currentPosition, -1, null);
  }

  public static SubtitleEditorDialogFragment newInstance(
      long currentPosition, int index, Subtitle editingSubtitle) {
    SubtitleEditorDialogFragment fragment = new SubtitleEditorDialogFragment();
    Bundle args = new Bundle();
    args.putLong(KEY_CURRENT_VIDEO_POSITION, currentPosition);
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

    viewModel = new ViewModelProvider(requireActivity()).get(SubtitlesViewModel.class);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    BottomSheetDialog sheetDialog = new BottomSheetDialog(requireContext());
    binding = FragmentDialogSubtitleEditorBinding.inflate(sheetDialog.getLayoutInflater());
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

    currentPosition = args.getLong(KEY_CURRENT_VIDEO_POSITION);

    if (args.containsKey(KEY_SELECTED_SUBTITLE) && args.containsKey(KEY_SELECTED_SUBTITLE_INDEX)) {
      editingSubtitle = BundleCompat.getParcelable(args, KEY_SELECTED_SUBTITLE, Subtitle.class);
      index = args.getInt(KEY_SELECTED_SUBTITLE_INDEX);
    } else {
      editingSubtitle = new Subtitle(currentPosition, currentPosition + 2000, "");
      index = -1;

      binding.deleteSubtitle.setVisibility(View.INVISIBLE);
    }
    previewSubtitile = editingSubtitle.clone();

    binding.currentVideoPosition.setText(
        getString(R.string.proj_current_video_position, TimeUtils.getTime(currentPosition)));
    binding.currentVideoPosition.setOnClickListener(
        v -> ClipboardUtils.copyText(TimeUtils.getTime(currentPosition)));
    binding.deleteSubtitle.setOnClickListener(v -> showAlertToDeleteSubtitle());

    binding.tieStartTime.setText(editingSubtitle.getStartTime().getTime());
    binding.tieEndTime.setText(editingSubtitle.getEndTime().getTime());
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
  }

  private void validateFields() {
    binding.dialogButtons.save.setEnabled(
        isValidSubtitleTime(binding.tieStartTime.getText().toString())
            && isValidSubtitleTime(binding.tieEndTime.getText().toString())
            && isValidSubtitleText(binding.tieText.getText().toString()));
  }

  private boolean isValidSubtitleTime(String time) {
    return TimeUtils.isValidTime(time.split(":"));
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
    editingSubtitle.setStartTime(
        new Time(TimeUtils.getMilliseconds(binding.tieStartTime.getText().toString())));
    editingSubtitle.setEndTime(
        new Time(TimeUtils.getMilliseconds(binding.tieEndTime.getText().toString())));
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

      if (sub.getStartTime().getMilliseconds() >= currentPosition) {
        index = i;
        break;
      }
    }
    return index;
  }
}
