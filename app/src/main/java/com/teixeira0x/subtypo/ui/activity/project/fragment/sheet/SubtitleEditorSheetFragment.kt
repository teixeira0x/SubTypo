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

package com.teixeira0x.subtypo.ui.activity.project.fragment.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.databinding.FragmentSubtitleEditorBinding
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.SubtitleEditorViewModel
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.SubtitleEditorViewModel.SubtitleEditorState
import com.teixeira0x.subtypo.ui.fragment.sheet.BaseBottomSheetFragment
import com.teixeira0x.subtypo.ui.utils.showConfirmDialog
import com.teixeira0x.subtypo.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubtitleEditorSheetFragment : BaseBottomSheetFragment() {

  companion object {

    @JvmStatic
    fun newInstance(
      subtitleId: Long = 0,
      projectId: Long = 0,
    ): SubtitleEditorSheetFragment {
      return SubtitleEditorSheetFragment().apply {
        arguments =
          Bundle().apply {
            putLong(Constants.KEY_SUBTITLE_ID_ARG, subtitleId)
            putLong(Constants.KEY_PROJECT_ID_ARG, projectId)
          }
      }
    }
  }

  private val subtitleEditorViewModel by viewModels<SubtitleEditorViewModel>()
  private var _binding: FragmentSubtitleEditorBinding? = null
  private val binding: FragmentSubtitleEditorBinding
    get() =
      checkNotNull(_binding) {
        "SubtitleEditorSheetFragment has been destroyed"
      }

  private var subtitleId: Long = 0
  private var projectId: Long = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      subtitleId = it.getLong(Constants.KEY_SUBTITLE_ID_ARG)
      projectId = it.getLong(Constants.KEY_PROJECT_ID_ARG)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentSubtitleEditorBinding.inflate(inflater)
      .also { _binding = it }
      .root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    configureListeners()
    observeViewModel()
    loadSubtitle()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun configureListeners() {
    binding.apply {
      deleteSubtitle.isEnabled = subtitleId > 0
      deleteSubtitle.setOnClickListener {
        requireContext().showConfirmDialog(
          title = R.string.delete,
          message = R.string.subtitle_remove_msg,
        ) { _, _ ->
          subtitleEditorViewModel.removeSubtitle(subtitleId)
        }
      }
      dialogButtons.cancel.setOnClickListener { dismiss() }
      dialogButtons.save.setOnClickListener {
        val name = binding.tieName.text.toString()
        if (subtitleId > 0) {
          subtitleEditorViewModel.updateSubtitle(
            subtitleId = subtitleId,
            name = name,
          )
        } else {
          subtitleEditorViewModel.insertSubtitle(
            projectId = projectId,
            name = name,
          )
        }
      }
    }
  }

  private fun loadSubtitle() {
    subtitleEditorViewModel.loadSubtitle(subtitleId)
  }

  private fun observeViewModel() {
    subtitleEditorViewModel.stateData.observe(this) { state ->
      when (state) {
        is SubtitleEditorState.Loading -> {}
        is SubtitleEditorState.Loaded -> onSubtitleLoaded(state)
        is SubtitleEditorState.Inserted,
        is SubtitleEditorState.Updated,
        is SubtitleEditorState.Removed -> {
          dismiss()
        }
        is SubtitleEditorState.Error -> {}
      }
    }
  }

  private fun onSubtitleLoaded(loadedState: SubtitleEditorState.Loaded) {
    val subtitle = loadedState.subtitle
    binding.title.setText(
      if (subtitle != null) R.string.subtitle_cue_edit
      else R.string.subtitle_cue_add
    )
    binding.tieName.setText(subtitle?.name ?: "subtitle")
  }
}
