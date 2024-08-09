package com.teixeira.subtitles.fragments.sheets;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.FrameLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.DialogFragment;
import com.blankj.utilcode.util.UriUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.activities.project.ProjectActivity;
import com.teixeira.subtitles.callbacks.UpdateProjectsCallback;
import com.teixeira.subtitles.databinding.FragmentConfigureProjectBinding;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.project.ProjectRepository;
import com.teixeira.subtitles.tasks.TaskExecutor;
import com.teixeira.subtitles.utils.FileUtil;
import com.teixeira.subtitles.utils.ToastUtils;
import com.teixeira.subtitles.utils.VideoUtils;
import java.io.File;

public class ConfigureProjectSheetFragment extends DialogFragment {

  public static ConfigureProjectSheetFragment newInstance() {
    return newInstance(null);
  }

  public static ConfigureProjectSheetFragment newInstance(Project project) {
    ConfigureProjectSheetFragment fragment = new ConfigureProjectSheetFragment();
    if (project != null) {
      Bundle args = new Bundle();
      args.putParcelable(ProjectActivity.KEY_PROJECT, project);
      fragment.setArguments(args);
    }
    return fragment;
  }

  private ActivityResultLauncher<String[]> videoDocumentPicker;
  private FragmentConfigureProjectBinding binding;
  private Project project = null;
  private File videoFile = null;

  private UpdateProjectsCallback callback;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof UpdateProjectsCallback) {
      callback = (UpdateProjectsCallback) context;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args != null && args.containsKey(ProjectActivity.KEY_PROJECT)) {
      project = BundleCompat.getParcelable(args, ProjectActivity.KEY_PROJECT, Project.class);
      videoFile = new File(project.getVideoPath());
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    BottomSheetDialog sheetDialog = new BottomSheetDialog(requireContext());
    binding = FragmentConfigureProjectBinding.inflate(sheetDialog.getLayoutInflater());
    sheetDialog.setContentView(binding.getRoot());

    BottomSheetBehavior<FrameLayout> behavior = sheetDialog.getBehavior();
    behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    init(savedInstanceState);
    return sheetDialog;
  }

  public void init(Bundle savedInstanceState) {
    videoDocumentPicker =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onChooseVideo);

    configureDetails();
    setListeners();

    binding.tieName.requestFocus();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    videoDocumentPicker.unregister();
    binding = null;
  }

  private void onChooseVideo(Uri uri) {
    if (uri != null) {
      videoFile = UriUtils.uri2File(uri);
      binding.videoIcon.setImageBitmap(VideoUtils.getVideoThumbnail(videoFile.getAbsolutePath()));
      binding.videoName.setText(videoFile.getName());
    }
  }

  private void configureDetails() {
    if (project != null) {
      binding.videoIcon.setImageBitmap(VideoUtils.getVideoThumbnail(videoFile.getAbsolutePath()));
      binding.videoName.setText(videoFile.getName());
      binding.tieName.setText(project.getName());
      binding.title.setText(R.string.proj_edit);
    } else {
      binding.title.setText(R.string.proj_new);
      binding.tieName.setText(R.string.proj_new);
    }
  }

  private void setListeners() {
    binding.chooseVideo.setOnClickListener(
        v -> videoDocumentPicker.launch(new String[] {"video/*"}));
    binding.dialogButtons.cancel.setOnClickListener(v -> dismiss());
    binding.dialogButtons.save.setOnClickListener(v -> writeProject());
  }

  private void writeProject() {

    if (videoFile == null) {
      ToastUtils.showShort(R.string.error_choose_video);
      return;
    }

    if (TextUtils.isEmpty(binding.tieName.getText().toString())) {
      ToastUtils.showShort(R.string.error_enter_name);
      return;
    }

    setCancelable(false);
    binding.dialogButtons.cancel.setClickable(false);
    binding.dialogButtons.save.setClickable(false);
    TaskExecutor.executeAsyncProvideError(
        () -> saveProjectInternal(),
        (result, throwable) -> {
          if (callback != null) {
            callback.updateProjects();
          }
          dismiss();
        });
  }

  private Void saveProjectInternal() {
    if (project != null) {
      ProjectRepository.writeProject(
          project.getProjectId(),
          binding.tieName.getText().toString(),
          videoFile.getAbsolutePath());

    } else {
      String projectId =
          ProjectRepository.writeProject(
              binding.tieName.getText().toString(), videoFile.getAbsolutePath());
      Intent intent = new Intent(requireContext(), ProjectActivity.class);
      intent.putExtra(
          ProjectActivity.KEY_PROJECT,
          new Project(
              projectId,
              FileUtil.PROJECTS_DIR + "/" + projectId,
              videoFile.getAbsolutePath(),
              binding.tieName.getText().toString()));
      startActivity(intent);
    }

    return null;
  }
}
