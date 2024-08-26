package com.teixeira.subtitles.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.FileUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira.subtitles.R
import com.teixeira.subtitles.activities.project.BaseProjectActivity
import com.teixeira.subtitles.activities.project.ProjectActivity
import com.teixeira.subtitles.adapters.ProjectListAdapter
import com.teixeira.subtitles.databinding.FragmentProjectsBinding
import com.teixeira.subtitles.fragments.dialogs.ProjectEditorDialogFragment
import com.teixeira.subtitles.models.Project
import com.teixeira.subtitles.utils.cancelIfActive
import com.teixeira.subtitles.viewmodels.ProjectsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectsFragment : Fragment() {

  private var _binding: FragmentProjectsBinding? = null
  private val binding: FragmentProjectsBinding
    get() = checkNotNull(_binding) { "ProjectsFragment has been destroyed!" }

  private val projectsViewModel by
    viewModels<ProjectsViewModel>(ownerProducer = { requireActivity() })
  private val coroutineScope = CoroutineScope(Dispatchers.IO)

  private lateinit var adapter: ProjectListAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentProjectsBinding.inflate(inflater, container, false).also { _binding = it }.root
  }

  override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
    super.onViewCreated(v, savedInstanceState)

    adapter =
      ProjectListAdapter({ _, project ->
        startActivity(
          Intent(requireContext(), ProjectActivity::class.java)
            .putExtra(BaseProjectActivity.KEY_PROJECT, project)
        )
      }) { view, project ->
        when (view.id) {
          R.id.edit_option ->
            ProjectEditorDialogFragment.newInstance(project).show(childFragmentManager, null)
          R.id.delete_option -> deleteProject(project)
          else -> {}
        }
      }

    binding.apply {
      projects.layoutManager = LinearLayoutManager(requireContext())
      projects.adapter = adapter
    }

    projectsViewModel.observeProjects(this) { projects ->
      binding.noProjects.isVisible = projects.isEmpty()
      adapter.submitList(projects)
    }
  }

  override fun onStart() {
    super.onStart()
    projectsViewModel.provideProjects()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    coroutineScope.cancelIfActive("Fragment has been destroyed!")
    _binding = null
  }

  private fun deleteProject(project: Project) {
    MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.delete)
      .setMessage(getString(R.string.msg_delete_confirmation, project.name))
      .setPositiveButton(R.string.yes) { _, _ -> deleteProjectAsync(project) }
      .setNegativeButton(R.string.no, null)
      .show()
  }

  private fun deleteProjectAsync(project: Project) {
    coroutineScope.launch {
      val deleted = FileUtils.delete(project.path)

      withContext(Dispatchers.Main) {
        if (deleted) {
          projectsViewModel.provideProjects()
        }
      }
    }
  }
}
