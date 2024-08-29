package com.teixeira.subtitles.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ClipboardUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira.subtitles.R
import com.teixeira.subtitles.databinding.FragmentDialogParagraphEditorBinding
import com.teixeira.subtitles.subtitle.models.Paragraph
import com.teixeira.subtitles.subtitle.models.Time
import com.teixeira.subtitles.subtitle.utils.TimeUtils
import com.teixeira.subtitles.utils.OnTextChangedListener
import com.teixeira.subtitles.utils.getParcelableCompat
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel

class ParagraphEditorDialogFragment : DialogFragment() {

  companion object {
    const val KEY_VIDEO_POSITION = "key_current_video_position"
    const val KEY_PARAGRAPH_INDEX = "key_paragraph_index"
    const val KEY_PARAGRAPH = "key_paragraph"

    val previewParagraph = Paragraph(Time(0), Time(0), "")

    @JvmStatic
    fun newInstance(
      videoPosition: Long,
      paragraphIndex: Int = -1,
      paragraph: Paragraph? = null,
    ): ParagraphEditorDialogFragment {
      return ParagraphEditorDialogFragment().also {
        it.arguments =
          Bundle().apply {
            putLong(KEY_VIDEO_POSITION, videoPosition)
            putInt(KEY_PARAGRAPH_INDEX, paragraphIndex)
            putParcelable(KEY_PARAGRAPH, paragraph)
          }
      }
    }
  }

  private var _binding: FragmentDialogParagraphEditorBinding? = null
  private val binding: FragmentDialogParagraphEditorBinding
    get() = checkNotNull(_binding) { "ParagraphEditorDialogFragment has been destroyed!" }

  private val subtitlesViewModel by
    viewModels<SubtitlesViewModel>(ownerProducer = { requireActivity() })

  private lateinit var paragraph: Paragraph

  private var videoPosition: Long = 0
  private var paragraphIndex = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val args = arguments ?: throw IllegalStateException("Arguments cannot be null")

    videoPosition = args.getLong(KEY_VIDEO_POSITION)
    paragraphIndex = args.getInt(KEY_PARAGRAPH_INDEX)
    paragraph =
      args.getParcelableCompat<Paragraph>(KEY_PARAGRAPH)
        ?: Paragraph(Time(videoPosition), Time(videoPosition + 2000), "")
    previewParagraph.text = paragraph.text
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val sheetDialog = BottomSheetDialog(requireContext())
    _binding = FragmentDialogParagraphEditorBinding.inflate(sheetDialog.layoutInflater)
    sheetDialog.setContentView(binding.root)

    sheetDialog.behavior.apply {
      peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
      state = BottomSheetBehavior.STATE_EXPANDED
    }

    binding.currentVideoPosition.setText(
      getString(R.string.proj_current_video_position, TimeUtils.getTime(videoPosition))
    )

    binding.currentVideoPosition.setOnClickListener {
      ClipboardUtils.copyText(TimeUtils.getTime(videoPosition))
    }
    binding.deleteParagraph.setOnClickListener { showAlertToDeleteParagraph() }
    binding.dialogButtons.cancel.setOnClickListener { dismiss() }
    binding.dialogButtons.save.setOnClickListener { saveParagraph() }

    binding.tieStartTime.setText(paragraph.startTime.time)
    binding.tieEndTime.setText(paragraph.endTime.time)
    binding.tieText.setText(paragraph.text)
    binding.preview.setParagraph(previewParagraph)
    configureTextWatchers()
    validateFields()

    return sheetDialog
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun configureTextWatchers() {

    val validateFieldsTextWatcher =
      object : OnTextChangedListener() {
        override fun afterTextChanged(editable: Editable) {
          validateFields()
        }
      }

    binding.tieStartTime.addTextChangedListener(validateFieldsTextWatcher)
    binding.tieEndTime.addTextChangedListener(validateFieldsTextWatcher)
    binding.tieText.addTextChangedListener(
      object : OnTextChangedListener() {

        override fun afterTextChanged(editable: Editable) {
          previewParagraph.text = editable.toString().trim()
          binding.preview.invalidate()
          validateFields()
        }
      }
    )
  }

  private fun validateFields() {
    binding.dialogButtons.save.isEnabled =
      isValidParagraphTime(binding.tieStartTime.text.toString()) &&
        isValidParagraphTime(binding.tieEndTime.text.toString()) &&
        isValidParagraphText(binding.tieText.text.toString())
  }

  private fun isValidParagraphTime(time: String): Boolean {
    return TimeUtils.isValidTime(time.split(":").toTypedArray())
  }

  private fun isValidParagraphText(text: String): Boolean {
    if (TextUtils.isEmpty(text)) {
      return false
    }

    val lines = text.trim().split("\n")
    for (line in lines) {
      if (TextUtils.isEmpty(line)) {
        return false
      }
    }
    return true
  }

  private fun showAlertToDeleteParagraph() {
    MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.delete)
      .setMessage(getString(R.string.msg_delete_confirmation, paragraph.text))
      .setPositiveButton(R.string.yes) { _, _ ->
        dismiss()
        subtitlesViewModel.removeParagraph(paragraphIndex)
      }
      .setNegativeButton(R.string.no, null)
      .show()
  }

  private fun saveParagraph() {
    val startTime = TimeUtils.getMilliseconds(binding.tieStartTime.text.toString())
    val endTime = TimeUtils.getMilliseconds(binding.tieEndTime.text.toString())
    val text = binding.tieText.text.toString()

    val hasParagraphChanged = hasParagraphChanged(paragraph, startTime, endTime, text)

    paragraph.apply {
      this.startTime.milliseconds = startTime
      this.endTime.milliseconds = endTime
      this.text = text
    }

    if (paragraphIndex >= 0) {
      if (hasParagraphChanged) {
        subtitlesViewModel.setParagraph(paragraphIndex, paragraph)
      }
    } else {
      subtitlesViewModel.addParagraph(getIndexForNewParagraph(), paragraph)
    }
    dismiss()
  }

  private fun hasParagraphChanged(
    paragraph: Paragraph,
    startTime: Long,
    endTime: Long,
    text: String,
  ): Boolean {
    return paragraph.startTime.milliseconds != startTime ||
      paragraph.endTime.milliseconds != endTime ||
      paragraph.text != text
  }

  private fun getIndexForNewParagraph(): Int {
    val paragraphs = subtitlesViewModel.paragraphs
    var index = paragraphs.size
    for (i in paragraphs.indices) {
      val paragraph = paragraphs[i]

      if (paragraph.startTime.milliseconds.toInt() >= videoPosition.toInt()) {
        index = i
        break
      }
    }
    return index
  }
}
