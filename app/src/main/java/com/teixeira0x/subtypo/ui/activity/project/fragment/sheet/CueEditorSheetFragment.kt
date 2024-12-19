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
import com.google.android.material.textfield.TextInputLayout
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.data.CueValidator
import com.teixeira0x.subtypo.data.ValidationResult
import com.teixeira0x.subtypo.databinding.FragmentCueEditorBinding
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.CueEditorViewModel
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.CueEditorViewModel.CueEditorState
import com.teixeira0x.subtypo.ui.fragment.sheet.BaseBottomSheetFragment
import com.teixeira0x.subtypo.ui.utils.EditTextUtils.afterTextChanged
import com.teixeira0x.subtypo.ui.utils.showConfirmDialog
import com.teixeira0x.subtypo.utils.Constants
import com.teixeira0x.subtypo.utils.TimeUtils.getFormattedTime
import com.teixeira0x.subtypo.utils.TimeUtils.getMilliseconds
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CueEditorSheetFragment : BaseBottomSheetFragment() {

  companion object {
    @JvmStatic
    fun newInstance(
      videoPosition: Long = 0L,
      subtitleId: Long,
      cueIndex: Int = -1,
    ): CueEditorSheetFragment {
      return CueEditorSheetFragment().apply {
        arguments =
          Bundle().apply {
            putLong(Constants.KEY_VIDEO_POSITION_ARG, videoPosition)
            putLong(Constants.KEY_SUBTITLE_ID_ARG, subtitleId)
            putInt(Constants.KEY_CUE_INDEX_ARG, cueIndex)
          }
      }
    }
  }

  private val cueViewModel by viewModels<CueEditorViewModel>()

  private var _binding: FragmentCueEditorBinding? = null
  private val binding: FragmentCueEditorBinding
    get() =
      checkNotNull(_binding) { "CueEditorSheetFragment has been destroyed" }

  private val cueValidator by lazy { CueValidator(requireContext()) }
  private var videoPosition: Long = 0L
  private var subtitleId: Long = 0L
  private var cueIndex: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      videoPosition = it.getLong(Constants.KEY_VIDEO_POSITION_ARG)
      subtitleId = it.getLong(Constants.KEY_SUBTITLE_ID_ARG)
      cueIndex = it.getInt(Constants.KEY_CUE_INDEX_ARG)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentCueEditorBinding.inflate(inflater)
      .also { _binding = it }
      .root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    observeViewModel()
    configureListeners()
    configureTextWatchers()
    loadCue()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun loadCue() {
    cueViewModel.loadCue(subtitleId, cueIndex)
  }

  private fun observeViewModel() {
    cueViewModel.stateData.observe(this) { state ->
      when (state) {
        is CueEditorState.Loading -> {}
        is CueEditorState.Loaded -> onCueLoaded(state)
        is CueEditorState.Removed,
        is CueEditorState.Inserted -> {
          dismiss()
        }
        is CueEditorState.Error -> {}
      }
    }
  }

  private fun onCueLoaded(loadedState: CueEditorState.Loaded) {
    val cue = loadedState.cue
    if (cue != null) {
      binding.title.setText(R.string.subtitle_cue_edit)
      binding.tieStartTime.setText(cue.startTime.getFormattedTime())
      binding.tieEndTime.setText(cue.endTime.getFormattedTime())
      binding.tieText.setText(cue.text)
    } else {
      binding.title.setText(R.string.subtitle_cue_add)
      binding.tieStartTime.setText(videoPosition.getFormattedTime())
      binding.tieEndTime.setText((videoPosition + 2000).getFormattedTime())
    }
    validateFields()
  }

  private fun configureListeners() {
    binding.apply {
      deleteCue.isEnabled = cueIndex >= 0
      deleteCue.setOnClickListener {
        requireContext().showConfirmDialog(
          title = R.string.remove,
          message = R.string.subtitle_cue_remove_msg,
        ) { _, _ ->
          cueViewModel.removeCue(subtitleId, cueIndex)
        }
      }
      dialogButtons.cancel.setOnClickListener { dismiss() }
      dialogButtons.save.setOnClickListener {
        if (!isValidFields()) {
          return@setOnClickListener
        }

        val startTime = binding.tieStartTime.text.toString().getMilliseconds()
        val endTime = binding.tieEndTime.text.toString().getMilliseconds()
        val text = binding.tieText.text.toString().trim()

        if (cueIndex >= 0) {
          cueViewModel.updateCue(
            subtitleId = subtitleId,
            cueIndex = cueIndex,
            startTime = startTime,
            endTime = endTime,
            text = text,
          )
        } else {
          cueViewModel.insertCue(
            subtitleId = subtitleId,
            startTime = startTime,
            endTime = endTime,
            text = text,
          )
        }
      }
    }
  }

  private fun configureTextWatchers() {
    binding.tieStartTime.afterTextChanged {
      validateTime(binding.tilStartTime, it.toString())
    }

    binding.tieEndTime.afterTextChanged {
      validateTime(binding.tilEndTime, it.toString())
    }

    binding.tieText.afterTextChanged {
      validateText(binding.tilText, it.toString())
    }
  }

  private fun validateFields() {
    binding.apply {
      validateTime(tilStartTime, tieStartTime.text.toString())
      validateTime(tilEndTime, tieEndTime.text.toString())
      validateText(tilText, tieText.text.toString())
    }
  }

  private fun validateTime(textInputLayout: TextInputLayout, timeText: String) {
    val result = cueValidator.checkTime(timeText)
    updateFieldError(textInputLayout, result)
  }

  private fun validateText(textInputLayout: TextInputLayout, text: String) {
    val result = cueValidator.checkText(text)
    updateFieldError(textInputLayout, result)
  }

  private fun updateFieldError(
    textInputLayout: TextInputLayout,
    result: ValidationResult,
  ) {
    if (result is ValidationResult.Error) {
      textInputLayout.setError(result.message)
      textInputLayout.isErrorEnabled = true
    } else {
      textInputLayout.isErrorEnabled = false
    }
    updateSaveButton()
  }

  private fun updateSaveButton() {
    binding.dialogButtons.save.isEnabled = isValidFields() && subtitleId > 0
  }

  private fun isValidFields(): Boolean {
    return (binding.tilStartTime.isErrorEnabled.not() &&
      binding.tilEndTime.isErrorEnabled.not() &&
      binding.tilText.isErrorEnabled.not())
  }
}
