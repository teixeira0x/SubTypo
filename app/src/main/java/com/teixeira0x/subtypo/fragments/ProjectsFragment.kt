package com.teixeira0x.subtypo.fragments

import android.content.Intent
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
import com.teixeira0x.subtypo.activities.project.BaseProjectActivity
import com.teixeira0x.subtypo.activities.project.ProjectActivity
import com.teixeira0x.subtypo.adapters.ProjectListAdapter
import com.teixeira0x.subtypo.databinding.FragmentProjectsBinding
import com.teixeira0x.subtypo.fragments.sheets.ProjectEditorFragment
import com.teixeira0x.subtypo.models.Project
import com.teixeira0x.subtypo.viewmodels.ProjectsViewModel
import com.teixeira0x.subtypo.viewmodels.ProjectsViewModel.ProjectsState.Loaded
import com.teixeira0x.subtypo.viewmodels.ProjectsViewModel.ProjectsState.Loading

class ProjectsFragment : Fragment() {

  private var _binding: FragmentProjectsBinding? = null
  private val binding: FragmentProjectsBinding
    get() = checkNotNull(_binding) { "ProjectsFragment has been destroyed!" }

  private val projectsViewModel by
    viewModels<ProjectsViewModel>(ownerProducer = { requireActivity() })

  private val projectsAdapter by lazy {
    ProjectListAdapter(
      onProjectClick = { _, project ->
        startActivity(
          Intent(requireContext(), ProjectActivity::class.java)
            .putExtra(BaseProjectActivity.KEY_PROJECT, project)
        )
      }
    ) { view, project ->
      when (view.id) {
        R.id.edit_option ->
          ProjectEditorFragment.newInstance(project)
            .show(childFragmentManager, null)
        R.id.delete_option -> deleteProject(project)
        else -> {}
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
    super.onViewCreated(v, savedInstanceState)

    projectsViewModel.stateData.observe(this) { state ->
      when (state) {
        is Loading -> {
          binding.projects.isVisible = false
          binding.noProjects.isVisible = false
        }
        is Loaded -> {
          binding.noProjects.isVisible = state.projects.isEmpty()
          binding.projects.isVisible = true
          projectsAdapter.submitList(state.projects)
        }
      }
    }

    binding.apply {
      projects.layoutManager = LinearLayoutManager(requireContext())
      projects.adapter = projectsAdapter
    }
  }

  override fun onStart() {
    super.onStart()
    projectsViewModel.loadProjects()
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
        projectsViewModel.deleteProject(project) {}
      }
      .setNegativeButton(R.string.no, null)
      .show()
  }
}
