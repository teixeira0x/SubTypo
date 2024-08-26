package com.teixeira.subtitles.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira.subtitles.R
import com.teixeira.subtitles.databinding.FragmentDialogTimedtextEditorBinding
import com.teixeira.subtitles.subtitle.formats.SubtitleFormat
import com.teixeira.subtitles.subtitle.models.TimedTextInfo
import com.teixeira.subtitles.subtitle.models.TimedTextObject
import com.teixeira.subtitles.utils.OnTextChangedListener
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel

class TimedTextEditorDialogFragment : DialogFragment() {

  private var _binding: FragmentDialogTimedtextEditorBinding? = null
  private val binding: FragmentDialogTimedtextEditorBinding
    get() = checkNotNull(_binding) { "Fragment has been destroyed!" }

  private val subtitlesViewModel by
    viewModels<SubtitlesViewModel>(ownerProducer = { requireActivity() })
  private var index: Int = -1
  private lateinit var timedTextObject: TimedTextObject

  private var isEditing = false

  companion object {
    fun newInstance(index: Int = -1): TimedTextEditorDialogFragment {
      val fragment = TimedTextEditorDialogFragment()
      if (index >= 0) {
        fragment.setArguments(Bundle().apply { putInt("index", index) })
      }
      return fragment
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val sheetDialog = BottomSheetDialog(requireContext())
    _binding = FragmentDialogTimedtextEditorBinding.inflate(sheetDialog.layoutInflater)
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
    arguments?.also {
      if (it.containsKey("index")) {
        index = it.getInt("index")
        timedTextObject = subtitlesViewModel.timedTextObjects[index]
        binding.delete.visibility = View.VISIBLE
        isEditing = true
      }
    } ?: run { timedTextObject = TimedTextObject(TimedTextInfo(SubtitleFormat.SUBRIP, "", "")) }

    binding.delete.setOnClickListener {
      MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.delete)
        .setMessage(getString(R.string.msg_delete_confirmation, timedTextObject.timedTextInfo.name))
        .setPositiveButton(
          R.string.yes,
          { _, _ ->
            dismiss()
            subtitlesViewModel.removeTimedTextObject(timedTextObject)
          },
        )
        .setNegativeButton(R.string.no, null)
        .show()
    }
    binding.title.setText(
      if (isEditing) R.string.proj_subtitle_lang_edit else R.string.proj_subtitle_lang_add
    )
    binding.tieName.setText(timedTextObject.timedTextInfo.name)
    binding.tieLanguage.setText(timedTextObject.timedTextInfo.language)
    binding.tieFormat.setText(timedTextObject.timedTextInfo.format.extension)
    binding.tieFormat.setAdapter(
      ArrayAdapter<String>(
        requireContext(),
        android.R.layout.simple_list_item_1,
        arrayOf(SubtitleFormat.SUBRIP.extension),
      )
    )
    setTextWatchers()

    binding.dialogButtons.cancel.setOnClickListener { dismiss() }
    binding.dialogButtons.save.setOnClickListener { onSave() }
  }

  private fun setTextWatchers() {
    binding.tieName.addTextChangedListener(
      object : OnTextChangedListener() {

        override fun afterTextChanged(editable: Editable) {
          verifyName()
          updateSaveButton()
        }
      }
    )
    binding.tieLanguage.addTextChangedListener(
      object : OnTextChangedListener() {

        override fun afterTextChanged(editable: Editable) {
          verifyLanguage()
          updateSaveButton()
        }
      }
    )
    verifyName()
    verifyLanguage()
    updateSaveButton()
  }

  private fun updateSaveButton() {
    val enableSaveButton = !binding.tilName.isErrorEnabled && !binding.tilLanguage.isErrorEnabled
    binding.dialogButtons.save.isEnabled = enableSaveButton
  }

  private fun verifyName() {
    val name = binding.tieName.text.toString().trim()

    when {
      name.length <= 0 -> binding.tilName.setError(getString(R.string.warn_text_field_cannot_empty))
      existsName(name) -> binding.tilName.setError(getString(R.string.warn_name_already_existing))
      else -> binding.tilName.setErrorEnabled(false)
    }
  }

  private fun verifyLanguage() {
    val language = binding.tieLanguage.text.toString().trim()
    if (language.length <= 0) {
      binding.tilLanguage.setError(getString(R.string.warn_text_field_cannot_empty))
    } else {
      binding.tilLanguage.setErrorEnabled(false)
    }
  }

  private fun existsName(name: String): Boolean {
    val timedTextObjects = subtitlesViewModel.timedTextObjects
    for (timedTextObject in timedTextObjects) {
      if (
        this.timedTextObject != timedTextObject && name.equals(timedTextObject.timedTextInfo.name)
      ) {
        return true
      }
    }
    return false
  }

  private fun onSave() {
    dismiss()

    timedTextObject.timedTextInfo.apply {
      name = binding.tieName.text.toString()
      language = binding.tieLanguage.text.toString()
      format = SubtitleFormat.getExtensionFormat(binding.tieFormat.text.toString())
    }

    if (isEditing) {
      subtitlesViewModel.setTimedTextObject(index, timedTextObject)
    } else {
      subtitlesViewModel.addTimedTextObject(timedTextObject, true)
    }
  }
}
