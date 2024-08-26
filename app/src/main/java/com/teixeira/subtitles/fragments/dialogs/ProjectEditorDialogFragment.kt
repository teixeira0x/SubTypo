package com.teixeira.subtitles.fragments.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.teixeira.subtitles.R
import com.teixeira.subtitles.activities.project.BaseProjectActivity
import com.teixeira.subtitles.activities.project.ProjectActivity
import com.teixeira.subtitles.databinding.FragmentDialogProjectEditorBinding
import com.teixeira.subtitles.models.Project
import com.teixeira.subtitles.project.ProjectRepository
import com.teixeira.subtitles.utils.ToastUtils
import com.teixeira.subtitles.utils.VideoUtils
import com.teixeira.subtitles.utils.cancelIfActive
import com.teixeira.subtitles.utils.getParcelableCompat
import com.teixeira.subtitles.viewmodels.ProjectEditorViewModel
import com.teixeira.subtitles.viewmodels.ProjectsViewModel
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectEditorDialogFragment : DialogFragment() {

  companion object {

    @JvmStatic
    fun newInstance(project: Project? = null): ProjectEditorDialogFragment {
      return ProjectEditorDialogFragment().also {
        if (project != null) {
          it.arguments = Bundle().apply { putParcelable(BaseProjectActivity.KEY_PROJECT, project) }
        }
      }
    }
  }

  private val projectsViewModel by
    viewModels<ProjectsViewModel>(ownerProducer = { requireActivity() })
  private val projectEditorViewModel by viewModels<ProjectEditorViewModel>()
  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  private val videoPicker =
    registerForActivityResult(ActivityResultContracts.OpenDocument(), this::onChooseVideo)
  private var binding: FragmentDialogProjectEditorBinding? = null

  private var videoUri: Uri? = null

  private var isExistingProject = false
  private var project: Project? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val args = arguments
    isExistingProject = args?.containsKey(BaseProjectActivity.KEY_PROJECT) ?: false
    if (isExistingProject) {
      project = args!!.getParcelableCompat<Project>(BaseProjectActivity.KEY_PROJECT)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val sheetDialog = BottomSheetDialog(requireContext())
    binding = FragmentDialogProjectEditorBinding.inflate(sheetDialog.layoutInflater)
    sheetDialog.setContentView(binding!!.root)

    sheetDialog.behavior.apply {
      peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
      state = BottomSheetBehavior.STATE_EXPANDED
    }

    if (isExistingProject) {
      binding!!.videoIcon.setImageBitmap(VideoUtils.getVideoThumbnail(project!!.videoPath))
      binding!!.videoName.setText(project!!.videoName)
      binding!!.tieName.setText(project!!.name)
      binding!!.title.setText(R.string.proj_edit)

      videoUri = Uri.fromFile(File(project!!.videoPath))
    } else {
      binding!!.title.setText(R.string.proj_new)
      binding!!.tieName.setText(R.string.proj_new)
    }

    binding!!.chooseVideo.setOnClickListener { videoPicker.launch(arrayOf("video/*")) }

    binding!!.dialogButtons.cancel.setOnClickListener {
      if (projectEditorViewModel.isCreatingProject) {
        coroutineScope.cancelIfActive("Cancel by user")
      }
      projectEditorViewModel.isCreatingProject = false
      dismiss()
    }

    binding!!.dialogButtons.save.setOnClickListener {
      if (!projectEditorViewModel.isCreatingProject) {
        createProject()
      }
    }

    projectEditorViewModel.observeCreatingProject(this) { isCreating ->
      binding?.progressIndicator?.isVisible = isCreating
      binding?.tieName?.setEnabled(!isCreating)
      binding?.dialogButtons?.save?.setEnabled(!isCreating)
      setCancelable(!isCreating)
    }
    return sheetDialog
  }

  override fun onDestroyView() {
    super.onDestroyView()
    videoPicker.unregister()
    binding = null
  }

  private fun onChooseVideo(uri: Uri?) {
    if (uri != null) {
      val videoDocument = DocumentFile.fromSingleUri(requireContext(), uri)
      binding?.videoIcon?.setImageBitmap(VideoUtils.getVideoThumbnailFromUri(uri))
      binding?.videoName?.setText(videoDocument?.name)
      this.videoUri = uri
    }
  }

  private fun createProject() {

    val binding = binding ?: return
    var videoUri = videoUri

    if (videoUri == null) {
      ToastUtils.showShort(R.string.error_choose_video)
      return
    }

    val name = binding.tieName.text.toString().trim()
    if (name.isEmpty()) {
      ToastUtils.showShort(R.string.error_enter_name)
      return
    }

    projectEditorViewModel.isCreatingProject = true

    coroutineScope.launch {
      var newProject: Project? = null

      if (isExistingProject) {
        ProjectRepository.updateProject(project!!.id, name, videoUri)
      } else {
        newProject = ProjectRepository.createProject(name, videoUri)
      }

      withContext(Dispatchers.Main) {
        dismiss()
        if (!isExistingProject) {
          startActivity(
            Intent(requireContext(), ProjectActivity::class.java)
              .putExtra(BaseProjectActivity.KEY_PROJECT, newProject)
          )
        }
        projectsViewModel.provideProjects()
      }
    }
  }
}
