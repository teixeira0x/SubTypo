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

package com.teixeira.subtitles.presentation.view.fragment.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira.subtitles.R
import com.teixeira.subtitles.databinding.FragmentDialogSubtitleEditorBinding
import com.teixeira.subtitles.presentation.view.viewmodel.SubtitlesViewModel
import com.teixeira.subtitles.subtitle.formats.SubtitleFormat
import com.teixeira.subtitles.subtitle.models.Subtitle
import com.teixeira.subtitles.utils.EditTextUtils.afterTextChanged

/**
 * BottomSheet to edit or add a new {@link Subtitle}.
 *
 * @author Felipe Teixeira
 */
class SubtitleEditorFragment : BaseBottomSheetFragment() {

  companion object {
    const val KEY_SUBTITLE_INDEX = "key_subtitle_index"

    @JvmStatic
    fun newInstance(subtitleIndex: Int = -1): SubtitleEditorFragment {
      return SubtitleEditorFragment().also {
        it.arguments = Bundle().apply { putInt(KEY_SUBTITLE_INDEX, subtitleIndex) }
      }
    }
  }

  private var _binding: FragmentDialogSubtitleEditorBinding? = null
  private val binding: FragmentDialogSubtitleEditorBinding
    get() = checkNotNull(_binding) { "Fragment has been destroyed!" }

  private val subtitlesViewModel by
    viewModels<SubtitlesViewModel>(ownerProducer = { requireActivity() })
  private var subtitleIndex: Int = -1
  private lateinit var subtitle: Subtitle

  private var isEditingSubtitle = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    subtitleIndex = arguments?.getInt(KEY_SUBTITLE_INDEX) ?: -1
    isEditingSubtitle = subtitleIndex >= 0
    subtitle =
      if (isEditingSubtitle) {
        subtitlesViewModel.subtitles[subtitleIndex]
      } else Subtitle()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentDialogSubtitleEditorBinding.inflate(inflater).also { _binding = it }.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.delete.setOnClickListener {
      MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.delete)
        .setMessage(getString(R.string.msg_delete_confirmation, subtitle.name))
        .setPositiveButton(
          R.string.yes,
          { _, _ ->
            dismiss()
            subtitlesViewModel.removeSubtitle(subtitle)
          },
        )
        .setNegativeButton(R.string.no, null)
        .show()
    }
    binding.delete.isVisible = isEditingSubtitle
    binding.title.setText(
      if (isEditingSubtitle) R.string.proj_subtitle_edit else R.string.proj_subtitle_add
    )
    binding.tieName.afterTextChanged { updateSaveButton() }
    binding.tieName.setText(subtitle.name)
    binding.tieFormat.setText(subtitle.subtitleFormat.extension)
    binding.tieFormat.setAdapter(
      ArrayAdapter<String>(
        requireContext(),
        android.R.layout.simple_list_item_1,
        SubtitleFormat.Builder.availableExtensions,
      )
    )

    binding.dialogButtons.cancel.setOnClickListener { dismiss() }
    binding.dialogButtons.save.setOnClickListener { onSave() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun updateSaveButton() {
    val name = binding.tieName.text.toString().trim()

    if (name.length <= 0) {
      binding.tilName.setError(getString(R.string.warn_text_field_cannot_empty))
    } else binding.tilName.setErrorEnabled(false)

    binding.dialogButtons.save.isEnabled = !binding.tilName.isErrorEnabled
  }

  private fun onSave() {

    subtitle.apply {
      name = binding.tieName.text.toString()
      subtitleFormat = SubtitleFormat.Builder.from(binding.tieFormat.text.toString()).build()
    }

    if (isEditingSubtitle) {
      subtitlesViewModel.setSubtitle(subtitleIndex, subtitle)
    } else {
      subtitlesViewModel.addSubtitle(subtitle, true)
    }
    dismiss()
  }
}
