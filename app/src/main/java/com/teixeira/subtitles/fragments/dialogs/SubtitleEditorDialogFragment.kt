package com.teixeira.subtitles.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira.subtitles.R
import com.teixeira.subtitles.databinding.FragmentDialogSubtitleEditorBinding
import com.teixeira.subtitles.subtitle.formats.SubtitleFormat
import com.teixeira.subtitles.subtitle.models.Subtitle
import com.teixeira.subtitles.utils.OnTextChangedListener
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel

class SubtitleEditorDialogFragment : DialogFragment() {

  private var _binding: FragmentDialogSubtitleEditorBinding? = null
  private val binding: FragmentDialogSubtitleEditorBinding
    get() = checkNotNull(_binding) { "Fragment has been destroyed!" }

  private val subtitlesViewModel by
    viewModels<SubtitlesViewModel>(ownerProducer = { requireActivity() })
  private var index: Int = -1
  private lateinit var subtitle: Subtitle

  private var isEditingSubtitle = false

  companion object {
    const val KEY_SUBTITLE_INDEX = "key_subtitle_index"

    @JvmStatic
    fun newInstance(index: Int = -1): SubtitleEditorDialogFragment {
      return SubtitleEditorDialogFragment().also {
        it.arguments = Bundle().apply { putInt(KEY_SUBTITLE_INDEX, index) }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val args = arguments
    index = args?.getInt(KEY_SUBTITLE_INDEX) ?: -1
    isEditingSubtitle = index >= 0
    subtitle =
      if (isEditingSubtitle) {
        subtitlesViewModel.subtitles[index]
      } else Subtitle()
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val sheetDialog = BottomSheetDialog(requireContext())
    _binding = FragmentDialogSubtitleEditorBinding.inflate(sheetDialog.layoutInflater)
    sheetDialog.setContentView(binding.root)
    sheetDialog.setCancelable(isCancelable())

    sheetDialog.behavior.apply {
      peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
      state = BottomSheetBehavior.STATE_EXPANDED
    }
    init()
    return sheetDialog
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun init() {

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
    binding.tieName.setText(subtitle.name)
    binding.tieFormat.setText(subtitle.subtitleFormat.extension)
    binding.tieFormat.setAdapter(
      ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, arrayOf(".srt"))
    )
    setTextWatchers()

    binding.dialogButtons.cancel.setOnClickListener { dismiss() }
    binding.dialogButtons.save.setOnClickListener { onSave() }
  }

  private fun setTextWatchers() {
    binding.tieName.addTextChangedListener(
      object : OnTextChangedListener() {

        override fun afterTextChanged(editable: Editable) {
          updateSaveButton()
        }
      }
    )
    updateSaveButton()
  }

  private fun updateSaveButton() {
    val name = binding.tieName.text.toString().trim()

    if (name.length <= 0) {
      binding.tilName.setError(getString(R.string.warn_text_field_cannot_empty))
    } else binding.tilName.setErrorEnabled(false)

    binding.dialogButtons.save.isEnabled = !binding.tilName.isErrorEnabled
  }

  private fun onSave() {
    dismiss()

    subtitle.apply {
      name = binding.tieName.text.toString()
      subtitleFormat = SubtitleFormat.getExtensionFormat(binding.tieFormat.text.toString())
    }

    if (isEditingSubtitle) {
      subtitlesViewModel.setSubtitle(index, subtitle)
    } else {
      subtitlesViewModel.addSubtitle(subtitle, true)
    }
  }
}
