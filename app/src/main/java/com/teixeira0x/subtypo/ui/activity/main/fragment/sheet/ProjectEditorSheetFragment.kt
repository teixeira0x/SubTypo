package com.teixeira0x.subtypo.ui.activity.main.fragment.sheet

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.UriUtils
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.databinding.FragmentDialogProjectEditorBinding
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.ui.activity.Navigator.navigateToProjectActivity
import com.teixeira0x.subtypo.ui.activity.main.permission.PermissionsHandler
import com.teixeira0x.subtypo.ui.activity.main.permission.PermissionsHandler.Companion.isPermissionsGranted
import com.teixeira0x.subtypo.ui.activity.main.viewmodel.ProjectEditorViewModel
import com.teixeira0x.subtypo.ui.activity.main.viewmodel.ProjectEditorViewModel.ProjectEditorState
import com.teixeira0x.subtypo.ui.fragment.sheet.BaseBottomSheetFragment
import com.teixeira0x.subtypo.ui.utils.VideoUtils.getVideoThumbnail
import com.teixeira0x.subtypo.ui.utils.showToastShort
import com.teixeira0x.subtypo.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

/**
 * BottomSheet to edit or create a new Project.
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

  private var projectId: Long = 0
  private var videoUri: Uri? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    projectId = arguments?.getLong(Constants.KEY_PROJECT_ID_ARG) ?: 0
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
    binding.chooseVideo.setOnClickListener {
      if (requireContext().isPermissionsGranted()) {
        videoPicker.launch(arrayOf("video/*"))
      } else {
        PermissionsHandler.showPermissionSettingsDialog(requireContext())
      }
    }

    binding.dialogButtons.cancel.setOnClickListener { dismiss() }
    binding.dialogButtons.save.setOnClickListener {
      if (isValidParams()) {
        viewModel.onCreating()
      }
    }
    viewModel.loadProject(projectId)
    observeViewModel()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    videoPicker.unregister()
    _binding = null
  }

  private fun observeViewModel() {
    viewModel.stateData.observe(this) { state ->
      when (state) {
        is ProjectEditorState.Loading -> onLoadingChange(true)
        is ProjectEditorState.Loaded -> {
          onLoadingChange(false)
          updateUI(state.project)
        }
        is ProjectEditorState.Creating -> createProject()
        is ProjectEditorState.Created -> {
          if (state.openProject) {
            navigateToProjectActivity(requireContext(), state.projectId)
          }
          dismiss()
        }
      }
    }
  }

  private fun updateUI(project: Project?) {
    if (project != null) {
      binding.videoThumbnail.setImageBitmap(
        requireContext().getVideoThumbnail(project.videoUri)
      )
      binding.videoName.setText(project.videoName)
      binding.tieName.setText(project.name)
      binding.title.setText(R.string.proj_edit)

      videoUri = Uri.fromFile(File(project.videoUri))
    } else {
      binding.title.setText(R.string.proj_new)
      binding.tieName.setText(R.string.proj_new)
    }
  }

  private fun createProject() {
    onLoadingChange(true)

    if (projectId > 0) {
      viewModel.updateProject(
        id = projectId,
        name = binding.tieName.text.toString().trim(),
        videoUri = UriUtils.uri2File(videoUri!!).absolutePath,
      )
    } else {
      viewModel.createProject(
        name = binding.tieName.text.toString().trim(),
        videoUri = UriUtils.uri2File(videoUri!!).absolutePath,
      )
    }
  }

  private fun onLoadingChange(isLoading: Boolean) {
    binding.chooseVideo.isClickable = !isLoading
    binding.tieName.isEnabled = !isLoading
    binding.dialogButtons.cancel.isEnabled = !isLoading
    binding.dialogButtons.save.isEnabled = !isLoading
    setCancelable(!isLoading)
  }

  private fun onChooseVideo(uri: Uri?) {
    if (uri != null) {
      val videoDocument = DocumentFile.fromSingleUri(requireContext(), uri)
      binding.videoThumbnail.setImageBitmap(
        requireContext().getVideoThumbnail(uri)
      )
      binding.videoName.setText(videoDocument?.name)
      this.videoUri = uri
    }
  }

  private fun isValidParams(): Boolean {
    val name = binding.tieName.text.toString().trim()

    return when {
      videoUri == null -> {
        requireContext().showToastShort(R.string.error_choose_video)
        false
      }
      name.isEmpty() -> {
        requireContext().showToastShort(R.string.error_enter_name)
        false
      }

      else -> true
    }
  }
}
