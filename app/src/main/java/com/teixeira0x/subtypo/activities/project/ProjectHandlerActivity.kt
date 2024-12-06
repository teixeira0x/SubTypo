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

package com.teixeira0x.subtypo.activities.project

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.adapters.SubtitleListAdapter
import com.teixeira0x.subtypo.databinding.LayoutSubtitlesDialogBinding
import com.teixeira0x.subtypo.fragments.sheets.ParagraphEditorFragment
import com.teixeira0x.subtypo.fragments.sheets.SubtitleEditorFragment
import com.teixeira0x.subtypo.models.Project
import com.teixeira0x.subtypo.project.ProjectManager
import com.teixeira0x.subtypo.project.ProjectRepository
import com.teixeira0x.subtypo.subtitle.models.Subtitle
import com.teixeira0x.subtypo.utils.BundleUtils.getParcelableCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base class for ProjectActivity that handles most project related things.
 *
 * @author Felipe Teixeira
 */
abstract class ProjectHandlerActivity : BaseProjectActivity() {

  protected lateinit var project: Project
  protected val projectManager = ProjectManager.getInstance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    project =
      intent?.extras?.getParcelableCompat<Project>(KEY_PROJECT)
        ?: throw IllegalArgumentException(
          "You cannot open this activity without a project."
        )

    projectManager.openProject(project)

    supportActionBar?.title = project.name
    initializeProject()
  }

  override fun preDestroy() {
    super.preDestroy()

    projectManager.destroy()
  }

  override fun postDestroy() {
    super.postDestroy()
  }

  private fun initializeProject() {
    supportActionBar?.setSubtitle(R.string.proj_initializing)
    coroutineScope.launch {
      var subtitles: MutableList<Subtitle>
      try {
        subtitles = ProjectRepository.getProjectSubtitles(project)
      } catch (e: Exception) {
        subtitles = ArrayList<Subtitle>()
        // Add handle to error.

      }

      withContext(Dispatchers.Main) {
        supportActionBar?.subtitle = project.videoName
        subtitlesViewModel.subtitles = subtitles
        onInitializeProject()
      }
    }
  }

  protected open fun onInitializeProject() {}

  protected fun showSubtitleEditorSheet(index: Int = -1) {
    SubtitleEditorFragment.newInstance(index).show(supportFragmentManager, "")
  }

  protected fun showParagraphEditorSheet(index: Int = -1) {
    ParagraphEditorFragment.newInstance(
        videoPosition = videoViewModel.currentPosition,
        paragraphIndex = index,
      )
      .show(supportFragmentManager, null)
  }

  protected fun showSubtitleSelectorDialog() {
    val binding = LayoutSubtitlesDialogBinding.inflate(layoutInflater)
    val builder = MaterialAlertDialogBuilder(this)
    builder.setPositiveButton(R.string.cancel, null)
    builder.setView(binding.root)
    val dialog = builder.show()

    binding.apply {
      this.addSubtitle.setOnClickListener {
        dialog.dismiss()
        showSubtitleEditorSheet()
      }

      if (subtitlesViewModel.subtitles.isEmpty()) {
        binding.noSubtitles.isVisible = true
        return@apply
      }
      this.subtitles.layoutManager =
        LinearLayoutManager(this@ProjectHandlerActivity)
      this.subtitles.adapter =
        SubtitleListAdapter(
          subtitlesViewModel.subtitles,
          subtitlesViewModel.subtitleIndex,
          { view, index, subtitle ->
            dialog.dismiss()
            when (view.id) {
              R.id.edit -> showSubtitleEditorSheet(index)
              else -> subtitlesViewModel.setCurrentSubtitle(index, subtitle)
            }
          },
        ) { index, _ ->
          dialog.dismiss()
          showSubtitleEditorSheet(index)
          true
        }
    }
  }

  protected fun saveProjectAsync() {
    supportActionBar?.setSubtitle(R.string.proj_saving)
    coroutineScope.launch(Dispatchers.IO) {
      try {
        ProjectRepository.writeSubtitleDataFile(
          project.path,
          subtitlesViewModel.subtitles,
        )
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          MaterialAlertDialogBuilder(this@ProjectHandlerActivity)
            .setTitle(getString(R.string.error_saving_project))
            .setMessage(e.toString())
            .setPositiveButton(R.string.ok, null)
            .show()
        }
      }

      withContext(Dispatchers.Main) {
        supportActionBar?.subtitle =
          subtitlesViewModel.subtitle?.let {
            "${subtitlesViewModel.subtitleIndex + 1}. ${it.fullName}"
          }
      }
    }
  }
}
