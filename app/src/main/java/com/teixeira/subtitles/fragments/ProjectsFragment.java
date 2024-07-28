package com.teixeira.subtitles.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blankj.utilcode.util.FileUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.activities.ProjectActivity;
import com.teixeira.subtitles.adapters.ProjectListAdapter;
import com.teixeira.subtitles.databinding.FragmentProjectsBinding;
import com.teixeira.subtitles.fragments.sheets.CreateProjectSheetFragment;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.project.ProjectRepository;
import com.teixeira.subtitles.tasks.TaskExecutor;
import java.util.List;

public class ProjectsFragment extends Fragment implements ProjectListAdapter.ProjectListener {

  public static final String FRAGMENT_TAG = "projects";

  private FragmentProjectsBinding binding;
  private ProjectListAdapter adapter;

  @NonNull
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentProjectsBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    adapter = new ProjectListAdapter(this);

    binding.projects.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.projects.setAdapter(adapter);
  }

  @Override
  public void onStart() {
    super.onStart();
    loadProjects();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    adapter = null;
    binding = null;
  }

  @Override
  public void onProjectClickListener(View view, Project project) {
    Intent intent = new Intent(requireContext(), ProjectActivity.class);
    intent.putExtra("project", project);
    startActivity(intent);
  }

  @Override
  public boolean onProjectLongClickListener(View view, Project project) {
    return true;
  }

  @Override
  public void onProjectMenuClickListener(View view, Project project) {
    PopupMenu pm = new PopupMenu(requireContext(), view);
    pm.getMenu().add(0, 0, 0, R.string.rename);
    pm.getMenu().add(0, 1, 0, R.string.delete);

    pm.setOnMenuItemClickListener(
        item -> {
          if (item.getItemId() == 0) {
            CreateProjectSheetFragment.newInstance(project).show(getChildFragmentManager(), null);
          } else deleteProject(project);
          return true;
        });
    pm.show();
  }

  public void loadProjects() {
    TaskExecutor.executeAsync(
        () -> ProjectRepository.fetchProjects(),
        (result) -> {
          List<Project> projects = (List<Project>) result;
          binding.noProjects.setVisibility(projects.isEmpty() ? View.VISIBLE : View.GONE);
          adapter.setProjects(projects);
        });
  }

  private void deleteProject(Project project) {
    new MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.delete)
        .setMessage(getString(R.string.delete_message, project.getName()))
        .setPositiveButton(R.string.yes, (d, w) -> deleteProjectAsync(project))
        .setNegativeButton(R.string.no, null)
        .show();
  }

  private void deleteProjectAsync(Project project) {
    TaskExecutor.executeAsync(
        () -> FileUtils.delete(project.getProjectPath()), result -> loadProjects());
  }
}
