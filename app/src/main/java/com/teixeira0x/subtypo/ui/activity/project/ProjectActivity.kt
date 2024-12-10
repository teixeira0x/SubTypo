/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.ui.activity.project

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.R.attr
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.databinding.ActivityProjectBinding
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.ui.activity.BaseActivity
import com.teixeira0x.subtypo.ui.activity.project.adapter.CueListAdapter
import com.teixeira0x.subtypo.ui.activity.project.player.PlayerControlLayoutHandler
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.ProjectViewModel
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.ProjectViewModel.ProjectState
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.VideoViewModel
import com.teixeira0x.subtypo.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectActivity : BaseActivity() {

  private val cueListAdapter by lazy { CueListAdapter() }

  private val viewModel by viewModels<ProjectViewModel>()

  private var _binding: ActivityProjectBinding? = null

  private val binding: ActivityProjectBinding
    get() = checkNotNull(_binding) { "Activity has been destroyed!" }

  override val statusBarColor: Int
    get() = MaterialColors.getColor(this, attr.colorOnSurfaceInverse, 0)

  override fun bindView(): View {
    return ActivityProjectBinding.inflate(layoutInflater)
      .also { _binding = it }
      .root
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    binding.toolbar.setNavigationOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }

    lifecycle.addObserver(
      PlayerControlLayoutHandler(
        binding.playerContent,
        binding.playerControllerContent,
        viewModels<VideoViewModel>().value,
      )
    )
    binding.rvCues.layoutManager = LinearLayoutManager(this)
    binding.rvCues.adapter = cueListAdapter

    val projectId = intent?.extras?.getLong(Constants.KEY_PROJECT_ID_ARG)
    viewModel.loadProject(projectId ?: 0)
    observeViewModel()
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_project_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    /*when (item.itemId) {
      R.id.menu_import -> subtitlePickerHandler.launchPicker()
      R.id.menu_export -> subtitleExporterHandler.launchExporter()
      else -> {}
    }*/

    return super.onOptionsItemSelected(item)
  }

  private fun observeViewModel() {
    viewModel.stateData.observe(this) { state ->
      when (state) {
        is ProjectState.Loading ->
          showProgressDialog(getString(R.string.proj_initializing))
        is ProjectState.Loaded -> {
          updateUI(state.project)
          dismissProgressDialog()
        }
        is ProjectState.Error -> {
          handleError(state.message, true)
          dismissProgressDialog()
        }
      }
    }
  }

  private fun updateUI(project: Project) {
    supportActionBar?.title = project.name
    binding.playerContent.videoView.setUri(project.videoUri)
    cueListAdapter.submitList(project.cues)
  }

  private fun handleError(message: Int, isCritical: Boolean = false) {
    MaterialAlertDialogBuilder(this)
      .setMessage(message)
      .setPositiveButton(android.R.string.ok) { dialog, _ ->
        dialog.dismiss()
        if (isCritical) finish()
      }
      .setCancelable(!isCritical)
      .show()

    // TODO: Add a new activity to handle error.
  }
}
