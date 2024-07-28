package com.teixeira.subtitles.fragments.sheets;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.activities.ProjectActivity;
import com.teixeira.subtitles.callbacks.UpdateProjectsCallback;
import com.teixeira.subtitles.databinding.FragmentConfigureProjectBinding;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.project.ProjectRepository;
import com.teixeira.subtitles.tasks.TaskExecutor;
import com.teixeira.subtitles.utils.Constants;
import com.teixeira.subtitles.utils.VideoUtils;
import java.io.File;

public class CreateProjectSheetFragment extends BottomSheetDialogFragment {

  public static CreateProjectSheetFragment newInstance() {
    return newInstance(null);
  }

  public static CreateProjectSheetFragment newInstance(Project project) {
    CreateProjectSheetFragment fragment = new CreateProjectSheetFragment();
    if (project != null) {
      Bundle args = new Bundle();
      args.putParcelable("project", project);
      fragment.setArguments(args);
    }
    return fragment;
  }

  private ActivityResultLauncher<String[]> videoDocumentPicker;
  private FragmentConfigureProjectBinding binding;
  private Project project = null;
  private File videoFile = null;

  private UpdateProjectsCallback callback;

  private boolean needChooseVideo = true;

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
    if (args != null && args.containsKey("project")) {
      project = args.getParcelable("project", Project.class);
      videoFile = new File(project.getVideoPath());
      needChooseVideo = false;
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentConfigureProjectBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    videoDocumentPicker =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onChooseVideo);

    configureDetails();
    setListeners();
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
    }
  }

  private void configureDetails() {
    if (project != null) {
      binding.videoIcon.setImageBitmap(VideoUtils.getVideoThumbnail(project.getVideoPath()));
      binding.tieName.setText(project.getName());
    } else {
      binding.tieName.setText(R.string.new_project);
    }
    binding.btnChooseVideo.setClickable(needChooseVideo);
  }

  private void setListeners() {
    binding.btnChooseVideo.setOnClickListener(
        v -> videoDocumentPicker.launch(new String[] {"video/mp4"}));
    binding.dialogButtons.cancel.setOnClickListener(v -> dismiss());
    binding.dialogButtons.save.setOnClickListener(v -> writeProject());
  }

  private void writeProject() {

    if (videoFile == null && needChooseVideo) {
      ToastUtils.showShort(R.string.error_choose_a_video);
      return;
    }

    if (TextUtils.isEmpty(binding.tieName.getText().toString())) {
      ToastUtils.showShort(R.string.error_enter_a_name);
      return;
    }

    setCancelable(false);
    binding.dialogButtons.cancel.setClickable(false);
    binding.dialogButtons.save.setClickable(false);
    TaskExecutor.executeAsyncProvideError(
        () -> saveProjectInternal(), (result, throwable) -> dismiss());
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
          "project",
          new Project(
              projectId,
              Constants.PROJECTS_DIR_PATH + "/" + projectId,
              videoFile.getAbsolutePath(),
              binding.tieName.getText().toString()));
      startActivity(intent);
    }

    if (callback != null) {
      callback.updateProjects();
    }

    return null;
  }
}
