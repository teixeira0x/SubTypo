package com.teixeira0x.subtypo.ui.activity.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.databinding.FragmentProjectsBinding
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.ui.activity.Navigator.navigateToProjectActivity
import com.teixeira0x.subtypo.ui.activity.main.adapter.ProjectListAdapter
import com.teixeira0x.subtypo.ui.activity.main.fragment.sheet.ProjectEditorSheetFragment
import com.teixeira0x.subtypo.ui.activity.main.viewmodel.ProjectsViewModel
import com.teixeira0x.subtypo.ui.activity.main.viewmodel.ProjectsViewModel.ProjectsState.Loaded
import com.teixeira0x.subtypo.ui.activity.main.viewmodel.ProjectsViewModel.ProjectsState.Loading
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectsFragment : Fragment() {

  private var _binding: FragmentProjectsBinding? = null
  private val binding: FragmentProjectsBinding
    get() = checkNotNull(_binding) { "ProjectsFragment has been destroyed!" }

  private val projectsViewModel by
    viewModels<ProjectsViewModel>(ownerProducer = { requireActivity() })

  private val projectsAdapter by lazy {
    ProjectListAdapter(
      onProjectClick = { _, project ->
        navigateToProjectActivity(requireContext(), project.id)
      }
    ) { view, project ->
      when (view.id) {
        R.id.edit_option -> {
          ProjectEditorSheetFragment.newInstance(project.id)
            .show(childFragmentManager, null)
        }
        R.id.delete_option -> deleteProject(project)
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentProjectsBinding.inflate(inflater, container, false)
      .also { _binding = it }
      .root
  }

  override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
    projectsViewModel.stateData.observe(this) { state ->
      when (state) {
        is Loading -> {
          binding.rvProjects.isVisible = false
          binding.noProjects.isVisible = false
          binding.progress.isVisible = true
        }
        is Loaded -> {
          binding.noProjects.isVisible = state.projects.isEmpty()
          binding.rvProjects.isVisible = true
          binding.progress.isVisible = false
          projectsAdapter.submitList(
            state.projects.sortedByDescending { it.id }
          )
        }
      }
    }

    binding.apply {
      rvProjects.layoutManager = LinearLayoutManager(requireContext())
      rvProjects.adapter = projectsAdapter
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun deleteProject(project: Project) {
    MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.delete)
      .setMessage(getString(R.string.msg_delete_confirmation, project.name))
      .setPositiveButton(R.string.yes) { _, _ ->
        projectsViewModel.deleteProject(project.id) {
          // TODO: Show a message to the user saying that the project has been
          // deleted.
        }
      }
      .setNegativeButton(R.string.no, null)
      .show()
  }
}
