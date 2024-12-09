package com.teixeira0x.subtypo.ui.activity.main.adapter

import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.databinding.LayoutProjectItemBinding
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.ui.adapter.holder.BindingViewHolder
import com.teixeira0x.subtypo.utils.ContextUtils.layoutInflater
import com.teixeira0x.subtypo.utils.UiUtils
import com.teixeira0x.subtypo.utils.VideoUtils

typealias ProjectViewHolder = BindingViewHolder<LayoutProjectItemBinding>

class ProjectListAdapter(
  val onProjectClick: (view: View, project: Project) -> Unit,
  val onProjectOptionClick: (view: View, project: Project) -> Unit,
) : RecyclerView.Adapter<ProjectViewHolder>() {

  companion object {
    const val DEFAULT_CHEVRON_ROTATION = -90f
  }

  private var projects = listOf<Project>()

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ): ProjectViewHolder {
    return ProjectViewHolder(
      LayoutProjectItemBinding.inflate(
        parent.context.layoutInflater,
        parent,
        false,
      )
    )
  }

  override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
    holder.binding.apply {
      val project = projects[position]

      videoIcon.setImageBitmap(VideoUtils.getVideoThumbnail(project.videoUri))
      videoName.setText(project.videoName)
      name.setText(project.name)

      root.setLayoutTransition(
        LayoutTransition().apply {
          enableTransitionType(LayoutTransition.CHANGING)
        }
      )

      root.setOnClickListener { onProjectClick(it, project) }
      root.setOnLongClickListener {
        toggleOptionsVisibility(optionsContainer, chevron)
        true
      }
      chevron.setOnClickListener {
        toggleOptionsVisibility(optionsContainer, chevron)
      }

      options.editOption.setOnClickListener {
        onProjectOptionClick(it, project)
      }

      options.deleteOption.setOnClickListener {
        onProjectOptionClick(it, project)
      }
    }
  }

  override fun getItemCount(): Int = projects.size

  fun submitList(newProjects: List<Project>) {
    val result =
      DiffUtil.calculateDiff(ProjectDiffCallback(projects, newProjects))
    this.projects = newProjects
    result.dispatchUpdatesTo(this)
  }

  private fun toggleOptionsVisibility(
    optionsContainer: FrameLayout,
    chevron: ImageView,
  ) {
    optionsContainer.isVisible = !optionsContainer.isVisible
    UiUtils.rotateView(
      chevron,
      if (optionsContainer.isVisible) 0f else DEFAULT_CHEVRON_ROTATION,
    )
  }

  class ProjectDiffCallback(
    val oldList: List<Project>,
    val newList: List<Project>,
  ) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
      return oldList[oldPosition] == newList[newPosition]
    }

    override fun areContentsTheSame(
      oldPosition: Int,
      newPosition: Int,
    ): Boolean {
      val oldProject = oldList[oldPosition]
      val newProject = newList[newPosition]

      return oldProject.id == newProject.id &&
        oldProject.name == newProject.name &&
        oldProject.videoUri == newProject.videoUri
    }
  }
}
