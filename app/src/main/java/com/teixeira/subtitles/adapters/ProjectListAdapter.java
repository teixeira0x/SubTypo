package com.teixeira.subtitles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.FileUtils;
import com.teixeira.subtitles.databinding.LayoutProjectItemBinding;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.utils.VideoUtils;
import java.util.Collections;
import java.util.List;

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.VH> {

  private List<Project> projects = Collections.emptyList();

  private final ProjectListener listener;

  public ProjectListAdapter(ProjectListener listener) {
    this.listener = listener;
  }

  @Override
  public VH onCreateViewHolder(ViewGroup parent, int viewType) {
    return new VH(
        LayoutProjectItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(VH holder, int position) {
    LayoutProjectItemBinding binding = holder.binding;
    Project project = projects.get(position);

    binding.videoIcon.setImageBitmap(VideoUtils.getVideoThumbnail(project.getVideoPath()));
    binding.name.setText(project.getName());
    binding.videoName.setText(FileUtils.getFileName(project.getVideoPath()));

    binding.getRoot().setOnClickListener(v -> listener.onProjectClickListener(v, project));
    binding.getRoot().setOnLongClickListener(v -> listener.onProjectLongClickListener(v, project));
    binding.menu.setOnClickListener(v -> listener.onProjectMenuClickListener(v, project));
  }

  @Override
  public int getItemCount() {
    return projects.size();
  }

  public void setProjects(@NonNull List<Project> projects) {
    DiffUtil.DiffResult result =
        DiffUtil.calculateDiff(new ProjectDiffCallback(this.projects, projects));
    this.projects = projects;
    result.dispatchUpdatesTo(this);
  }

  public interface ProjectListener {

    void onProjectClickListener(View view, Project project);

    boolean onProjectLongClickListener(View view, Project project);

    void onProjectMenuClickListener(View view, Project project);
  }

  class VH extends RecyclerView.ViewHolder {
    private final LayoutProjectItemBinding binding;

    public VH(LayoutProjectItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  class ProjectDiffCallback extends DiffUtil.Callback {

    private final List<Project> oldList, newList;

    private ProjectDiffCallback(List<Project> oldList, List<Project> newList) {
      this.oldList = oldList;
      this.newList = newList;
    }

    @Override
    public int getOldListSize() {
      return oldList.size();
    }

    @Override
    public int getNewListSize() {
      return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldPosition, int newPosition) {
      Project oldProject = oldList.get(oldPosition);
      Project newProject = newList.get(newPosition);

      return oldProject.getProjectId().equals(newProject.getProjectId());
    }

    @Override
    public boolean areContentsTheSame(int oldPosition, int newPosition) {
      Project oldProject = oldList.get(oldPosition);
      Project newProject = newList.get(newPosition);

      return oldProject.getProjectPath().equals(newProject.getProjectPath())
          && oldProject.getVideoPath().equals(newProject.getVideoPath())
          && oldProject.getName().equals(newProject.getName());
    }
  }
}
