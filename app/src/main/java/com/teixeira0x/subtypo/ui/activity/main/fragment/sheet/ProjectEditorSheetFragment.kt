package com.teixeira0x.subtypo.ui.activity.main.fragment.sheet

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.UriUtils
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.databinding.FragmentDialogProjectEditorBinding
import com.teixeira0x.subtypo.ui.activity.Navigator.navigateToProjectActivity
import com.teixeira0x.subtypo.ui.activity.main.handler.PermissionsHandler
import com.teixeira0x.subtypo.ui.activity.main.viewmodel.ProjectEditorViewModel
import com.teixeira0x.subtypo.ui.fragment.sheet.BaseBottomSheetFragment
import com.teixeira0x.subtypo.utils.Constants
import com.teixeira0x.subtypo.utils.ToastUtils
import com.teixeira0x.subtypo.utils.VideoUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

/**
 * BottomSheet to edit or create a new {@link Project}.
 *
 * @author Felipe Teixeira
 */
@AndroidEntryPoint
class ProjectEditorSheetFragment : BaseBottomSheetFragment() {

  companion object {

    @JvmStatic
    fun newInstance(projectId: Long = 0): ProjectEditorSheetFragment {
      return ProjectEditorSheetFragment().also {
        it.arguments =
          Bundle().apply { putLong(Constants.KEY_PROJECT_ID_ARG, projectId) }
      }
    }
  }

  private var _binding: FragmentDialogProjectEditorBinding? = null
  private val binding: FragmentDialogProjectEditorBinding
    get() =
      checkNotNull(_binding) {
        "ProjectEditorSheetFragment has been destroyed!"
      }

  private val viewModel by viewModels<ProjectEditorViewModel>()

  private val videoPicker =
    registerForActivityResult(
      ActivityResultContracts.OpenDocument(),
      this::onChooseVideo,
    )

  private var isExistingProject = false
  private var projectId: Long = 0
  private var videoUri: Uri? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    projectId = arguments?.getLong(Constants.KEY_PROJECT_ID_ARG) ?: 0
    isExistingProject = projectId > 0
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentDialogProjectEditorBinding.inflate(inflater)
      .also { _binding = it }
      .root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    configureListeners()
    updateFields()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    videoPicker.unregister()
    _binding = null
  }

  private fun updateFields() {
    if (isExistingProject) {
      viewModel.getProject(projectId) { project ->
        binding.videoThumbnail.setImageBitmap(
          VideoUtils.getVideoThumbnail(project.videoUri)
        )
        binding.videoName.setText(project.videoName)
        binding.tieName.setText(project.name)
        binding.title.setText(R.string.proj_edit)

        videoUri = Uri.fromFile(File(project.videoUri))
      }
    } else {
      binding.title.setText(R.string.proj_new)
      binding.tieName.setText(R.string.proj_new)
    }
  }

  private fun configureListeners() {
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

  private fun onChooseVideo(uri: Uri?) {
    if (uri != null) {
      val videoDocument = DocumentFile.fromSingleUri(requireContext(), uri)
      binding.videoThumbnail.setImageBitmap(
        VideoUtils.getVideoThumbnailFromUri(uri)
      )
      binding.videoName.setText(videoDocument?.name)
      this.videoUri = uri
    }
  }

  private fun createProject() {
    if (
      isExistingProject &&
        viewModel.stateData.value ==
          ProjectEditorViewModel.ProjectEditorState.Loading
    ) {
      return
    }

    var uri = videoUri
    val name = binding.tieName.text.toString().trim()

    when {
      videoUri == null -> ToastUtils.showShort(R.string.error_choose_video)
      name.isEmpty() -> ToastUtils.showShort(R.string.error_enter_name)

      else -> {
        val videoUri = UriUtils.uri2File(uri).absolutePath
        isProjectCreating(true)
        if (isExistingProject) {
          viewModel.updateProject(
            id = projectId,
            name = name,
            videoUri = videoUri,
          ) {
            dismiss()
          }
        } else {
          viewModel.createProject(name = name, videoUri = videoUri) { id ->
            navigateToProjectActivity(requireContext(), id)
            dismiss()
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
