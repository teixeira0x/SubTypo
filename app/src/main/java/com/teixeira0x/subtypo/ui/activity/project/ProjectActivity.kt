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
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.R.attr
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.databinding.ActivityProjectBinding
import com.teixeira0x.subtypo.ui.activity.BaseEdgeToEdgeActivity
import com.teixeira0x.subtypo.ui.activity.project.adapter.CueListAdapter
import com.teixeira0x.subtypo.ui.activity.project.fragment.SubtitleListFragment
import com.teixeira0x.subtypo.ui.activity.project.fragment.sheet.CueEditorSheetFragment
import com.teixeira0x.subtypo.ui.activity.project.player.PlayerControlLayoutHandler
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.ProjectViewModel
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.ProjectViewModel.ProjectState
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.SubtitleViewModel
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.SubtitleViewModel.SubtitleState
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.VideoViewModel
import com.teixeira0x.subtypo.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectActivity : BaseEdgeToEdgeActivity() {

  private lateinit var playerControlLayoutHandler: PlayerControlLayoutHandler

  private val projectViewModel by viewModels<ProjectViewModel>()
  private val videoViewModel by viewModels<VideoViewModel>()
  private val subtitleViewModel by viewModels<SubtitleViewModel>()

  private var _binding: ActivityProjectBinding? = null

  private val binding: ActivityProjectBinding
    get() = checkNotNull(_binding) { "Activity has been destroyed!" }

  private val cueListAdapter by lazy {
    CueListAdapter { index, _ ->
      val subtitleId = subtitleViewModel.selectedSubtitleId
      if (subtitleId > 0) {
        CueEditorSheetFragment.newInstance(
            videoViewModel.videoPosition.value!!,
            subtitleId,
            index,
          )
          .show(supportFragmentManager, null)
      }
    }
  }

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

    playerControlLayoutHandler =
      PlayerControlLayoutHandler(
        binding.playerContent,
        binding.playerControllerContent,
        videoViewModel,
      )

    lifecycle.addObserver(playerControlLayoutHandler)

    initialize()
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

  override fun onApplySystemBarInsets(insets: Insets) {
    _binding?.apply {
      appBar.updatePadding(top = insets.top)
      toolbar.updatePaddingRelative(start = insets.left, end = insets.right)

      mainContent.updatePadding(
        left = insets.left,
        right = insets.right,
        bottom = insets.bottom,
      )

      fragmentSubtitles
        .getFragment<SubtitleListFragment>()
        .onApplySystemBarInsets(insets)
    }
  }

  private fun initialize() {
    binding.rvCues.layoutManager = LinearLayoutManager(this)
    binding.rvCues.adapter = cueListAdapter
    observeViewModel()
    configureListeners()
    loadProject()
  }

  private fun configureListeners() {
    binding.fabAddCue.setOnClickListener {
      val subtitleId = subtitleViewModel.selectedSubtitleId
      if (subtitleId > 0) {
        CueEditorSheetFragment.newInstance(
            videoViewModel.videoPosition.value!!,
            subtitleId,
          )
          .show(supportFragmentManager, null)
      }
    }
  }

  private fun loadProject() {
    val projectId = intent?.extras?.getLong(Constants.KEY_PROJECT_ID_ARG)
    projectViewModel.loadProject(projectId ?: 0)
    subtitleViewModel.loadSubtitles(projectId ?: 0)
  }

  private fun observeViewModel() {
    projectViewModel.stateData.observe(this) { state ->
      when (state) {
        is ProjectState.Loading ->
          showProgressDialog(getString(R.string.proj_initializing))
        is ProjectState.Loaded -> {
          onProjectLoaded(state)
          dismissProgressDialog()
        }
        is ProjectState.Error -> {
          handleError(state.message, true)
          dismissProgressDialog()
        }
      }
    }

    subtitleViewModel.stateData.observe(this) { state ->
      when (state) {
        is SubtitleState.Loading -> {
          binding.cuesLoading.isVisible = true
          binding.rvCues.isVisible = false
          binding.noCues.isVisible = false
        }
        is SubtitleState.Loaded -> {
          onSubtitlesLoaded(state)
        }
        else -> {}
      }
    }
  }

  private fun onProjectLoaded(stateLoaded: ProjectState.Loaded) {
    val project = stateLoaded.project
    supportActionBar?.title = project.name
    binding.playerContent.videoView.setUri(project.videoUri)
  }

  private fun onSubtitlesLoaded(stateLoaded: SubtitleState.Loaded) {
    val selectedSubtitle = stateLoaded.selectedSubtitle
    val cues = selectedSubtitle?.cues ?: emptyList()

    supportActionBar?.subtitle = selectedSubtitle?.name
    playerControlLayoutHandler.setCues(cues)
    cueListAdapter.submitList(cues)

    binding.cuesLoading.isVisible = false
    binding.rvCues.isVisible = true
    binding.noCues.isVisible = cues.isEmpty()
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
