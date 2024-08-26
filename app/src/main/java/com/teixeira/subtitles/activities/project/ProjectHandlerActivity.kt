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

package com.teixeira.subtitles.activities.project

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira.subtitles.R
import com.teixeira.subtitles.adapters.LanguageListAdapter
import com.teixeira.subtitles.databinding.LayoutLanguagesDialogBinding
import com.teixeira.subtitles.fragments.dialogs.TimedTextEditorDialogFragment
import com.teixeira.subtitles.models.Project
import com.teixeira.subtitles.project.ProjectManager
import com.teixeira.subtitles.project.ProjectRepository
import com.teixeira.subtitles.subtitle.models.TimedTextObject
import com.teixeira.subtitles.utils.DialogUtils
import com.teixeira.subtitles.utils.getParcelableCompat
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

    if (intent == null || intent.extras == null) {
      throw IllegalArgumentException("You cannot open this activity without a project.")
    }

    val extras = intent.extras!!
    if (!extras.containsKey(KEY_PROJECT)) {
      throw IllegalArgumentException("You cannot open this activity without a project.")
    }

    project = extras.getParcelableCompat<Project>(KEY_PROJECT)!!
    projectManager.openProject(project)

    supportActionBar?.title = project.name
    supportActionBar?.subtitle = project.videoName
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
      var timedTextObjects: MutableList<TimedTextObject>
      try {
        timedTextObjects = ProjectRepository.getProjectTimedTextObjects(project)
      } catch (e: Exception) {
        timedTextObjects = ArrayList()
        // Add handle to error.
        withContext(Dispatchers.Main) {
          DialogUtils.createSimpleDialog(
              this@ProjectHandlerActivity,
              "An error ocurred",
              e.toString(),
            )
            .setPositiveButton(R.string.ok, null)
            .show()
        }
      }

      withContext(Dispatchers.Main) {
        supportActionBar?.subtitle = project.videoName

        subtitlesViewModel.timedTextObjects = timedTextObjects
        onInitializeProject()
      }
    }
  }

  protected open fun onInitializeProject() {
    val timedTextObjects = subtitlesViewModel.timedTextObjects
    if (!timedTextObjects.isEmpty()) {
      subtitlesViewModel.setSelectedTimedTextObject(0, timedTextObjects[0])
    }
    subtitlesViewModel.saveSubtitles = true
  }

  protected fun showTimedTextEditorDialog(index: Int = -1) {
    TimedTextEditorDialogFragment.newInstance(index).show(supportFragmentManager, "")
  }

  protected fun showLanguageSelectorDialog() {
    val binding = LayoutLanguagesDialogBinding.inflate(layoutInflater)
    val builder = MaterialAlertDialogBuilder(this)
    builder.setPositiveButton(R.string.cancel, null)
    builder.setView(binding.root)
    val dialog = builder.show()

    binding.apply {
      addLanguage.setOnClickListener {
        dialog.dismiss()
        showTimedTextEditorDialog()
      }
      val timedTextObjects = subtitlesViewModel.timedTextObjects
      if (timedTextObjects.isEmpty()) {
        binding.noLanguages.isVisible = true
        return@apply
      }

      languages.layoutManager = LinearLayoutManager(this@ProjectHandlerActivity)
      languages.adapter =
        LanguageListAdapter(
          subtitlesViewModel.timedTextObjects,
          subtitlesViewModel.selectedTimedTextObjectIndex,
          { view, index, timedTextObject ->
            dialog.dismiss()
            when (view.id) {
              R.id.edit -> showTimedTextEditorDialog(index)
              else -> subtitlesViewModel.setSelectedTimedTextObject(index, timedTextObject)
            }
          },
        ) { index, _ ->
          dialog.dismiss()
          showTimedTextEditorDialog(index)
          true
        }
    }
  }

  protected fun saveProjectAsync() {
    supportActionBar?.setSubtitle(R.string.proj_saving)

    coroutineScope.launch(Dispatchers.IO) {
      try {
        ProjectRepository.writeSubtitleDataFile(project.path, subtitlesViewModel.timedTextObjects)
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          DialogUtils.createSimpleDialog(
              this@ProjectHandlerActivity,
              getString(R.string.error_saving_project),
              e.toString(),
            )
            .setPositiveButton(R.string.ok, null)
            .show()
        }
      }

      withContext(Dispatchers.Main) { supportActionBar?.subtitle = project.videoName }
    }
  }
}
