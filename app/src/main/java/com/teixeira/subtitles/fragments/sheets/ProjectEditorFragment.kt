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

package com.teixeira.subtitles.fragments.sheets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import com.teixeira.subtitles.R
import com.teixeira.subtitles.activities.project.BaseProjectActivity
import com.teixeira.subtitles.activities.project.ProjectActivity
import com.teixeira.subtitles.databinding.FragmentDialogProjectEditorBinding
import com.teixeira.subtitles.handlers.PermissionsHandler
import com.teixeira.subtitles.models.Project
import com.teixeira.subtitles.utils.BundleUtils.getParcelableCompat
import com.teixeira.subtitles.utils.ToastUtils
import com.teixeira.subtitles.utils.VideoUtils
import com.teixeira.subtitles.viewmodels.ProjectsViewModel
import java.io.File
import kotlinx.coroutines.launch

/**
 * BottomSheet to edit or create a new {@link Project}.
 *
 * @author Felipe Teixeira
 */
class ProjectEditorFragment : BaseBottomSheetFragment() {

  companion object {

    @JvmStatic
    fun newInstance(project: Project? = null): ProjectEditorFragment {
      return ProjectEditorFragment().also {
        it.arguments = Bundle().apply { putParcelable(BaseProjectActivity.KEY_PROJECT, project) }
      }
    }
  }

  private var _binding: FragmentDialogProjectEditorBinding? = null
  private val binding: FragmentDialogProjectEditorBinding
    get() = checkNotNull(_binding) { "ProjectEditorFragment has been destroyed!" }

  private val projectsViewModel by
    viewModels<ProjectsViewModel>(ownerProducer = { requireActivity() })

  private val videoPicker =
    registerForActivityResult(ActivityResultContracts.OpenDocument(), this::onChooseVideo)

  private var isExistingProject = false
  private var project: Project? = null
  private var videoUri: Uri? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    project = arguments?.getParcelableCompat<Project>(BaseProjectActivity.KEY_PROJECT)
    isExistingProject = project != null
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentDialogProjectEditorBinding.inflate(inflater).also { _binding = it }.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    if (isExistingProject) {
      binding.videoIcon.setImageBitmap(VideoUtils.getVideoThumbnail(project!!.videoPath))
      binding.videoName.setText(project!!.videoName)
      binding.tieName.setText(project!!.name)
      binding.title.setText(R.string.proj_edit)

      videoUri = Uri.fromFile(File(project!!.videoPath))
    } else {
      binding.title.setText(R.string.proj_new)
      binding.tieName.setText(R.string.proj_new)
    }

    binding.chooseVideo.setOnClickListener {
      if (PermissionsHandler.isPermissionsGranted(requireContext())) {
        videoPicker.launch(arrayOf("video/*"))
      } else {
        PermissionsHandler.showPermissionSettingsDialog(requireContext())
      }
    }

    binding.dialogButtons.cancel.setOnClickListener { dismiss() }
    binding.dialogButtons.save.setOnClickListener { createProject() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    videoPicker.unregister()
    _binding = null
  }

  private fun onChooseVideo(uri: Uri?) {
    if (uri != null) {
      val videoDocument = DocumentFile.fromSingleUri(requireContext(), uri)
      binding.videoIcon.setImageBitmap(VideoUtils.getVideoThumbnailFromUri(uri))
      binding.videoName.setText(videoDocument?.name)
      this.videoUri = uri
    }
  }

  private fun createProject() {

    var videoUri = videoUri
    val name = binding.tieName.text.toString().trim()

    when {
      videoUri == null -> ToastUtils.showShort(R.string.error_choose_video)
      name.isEmpty() -> ToastUtils.showShort(R.string.error_enter_name)

      else -> {
        isProjectCreating(true)
        if (isExistingProject) {
          projectsViewModel.updateProject(project!!.id, name, videoUri) { dismiss() }
        } else {
          projectsViewModel.createProject(name, videoUri) {
            dismiss()
            startActivity(
              Intent(requireContext(), ProjectActivity::class.java)
                .putExtra(BaseProjectActivity.KEY_PROJECT, it)
            )
          }
        }
      }
    }
  }

  private fun isProjectCreating(isCreating: Boolean) {
    binding.progressIndicator.isVisible = isCreating
    binding.tieName.isEnabled = !isCreating
    binding.dialogButtons.cancel.isEnabled = !isCreating
    binding.dialogButtons.save.isEnabled = !isCreating
    setCancelable(!isCreating)
  }
}
