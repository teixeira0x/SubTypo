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

package com.teixeira0x.subtypo.fragments.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ClipboardUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.data.ParagraphValidator
import com.teixeira0x.subtypo.data.ValidationResult
import com.teixeira0x.subtypo.databinding.FragmentDialogParagraphEditorBinding
import com.teixeira0x.subtypo.subtitle.models.Paragraph
import com.teixeira0x.subtypo.subtitle.models.Subtitle
import com.teixeira0x.subtypo.subtitle.models.Time
import com.teixeira0x.subtypo.subtitle.utils.TimeUtils.getFormattedTime
import com.teixeira0x.subtypo.subtitle.utils.TimeUtils.getMilliseconds
import com.teixeira0x.subtypo.utils.EditTextUtils.afterTextChanged
import com.teixeira0x.subtypo.viewmodels.SubtitlesViewModel

/**
 * BottomSheet to edit or add a new {@link Paragraph}.
 *
 * @author Felipe Teixeira
 */
class ParagraphEditorFragment : BaseBottomSheetFragment() {

  companion object {
    private val previewSubtitle =
      Subtitle(paragraphs = mutableListOf(Paragraph(Time(0), Time(0), "")))

    const val KEY_VIDEO_POSITION = "key_current_video_position"
    const val KEY_PARAGRAPH_INDEX = "key_paragraph_index"

    const val DEFAULT_PARAGRAPH_DURATION = 2000

    @JvmStatic
    fun newInstance(
      videoPosition: Long,
      paragraphIndex: Int = -1,
    ): ParagraphEditorFragment {
      return ParagraphEditorFragment().also {
        it.arguments =
          Bundle().apply {
            putLong(KEY_VIDEO_POSITION, videoPosition)
            putInt(KEY_PARAGRAPH_INDEX, paragraphIndex)
          }
      }
    }
  }

  private var _binding: FragmentDialogParagraphEditorBinding? = null
  private val subtitlesViewModel by
    viewModels<SubtitlesViewModel>(ownerProducer = { requireActivity() })
  private val paragraphValidator by lazy {
    ParagraphValidator(requireContext())
  }

  private var videoPosition: Long = 0
  private var paragraphIndex = -1
  private lateinit var unmodifiedParagraph: Paragraph

  private val binding: FragmentDialogParagraphEditorBinding
    get() =
      checkNotNull(_binding) { "ParagraphEditorFragment has been destroyed!" }

  private val isExistingParagraph: Boolean
    get() = paragraphIndex >= 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val arguments = checkNotNull(arguments) { "Arguments cannot be null" }

    videoPosition = arguments.getLong(KEY_VIDEO_POSITION)
    paragraphIndex = arguments.getInt(KEY_PARAGRAPH_INDEX)
    unmodifiedParagraph =
      if (isExistingParagraph) subtitlesViewModel.paragraphs[paragraphIndex]
      else
        Paragraph(
          Time(videoPosition),
          Time(videoPosition + DEFAULT_PARAGRAPH_DURATION),
          "",
        )
    previewSubtitle.paragraphs[0].text = unmodifiedParagraph.text
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentDialogParagraphEditorBinding.inflate(inflater)
      .also { _binding = it }
      .root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    binding.deleteParagraph.isEnabled = isExistingParagraph
    binding.deleteParagraph.setOnClickListener {
      MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.delete)
        .setMessage(
          getString(R.string.msg_delete_confirmation, unmodifiedParagraph.text)
        )
        .setPositiveButton(R.string.yes) { _, _ ->
          dismiss()
          subtitlesViewModel.removeParagraph(paragraphIndex)
        }
        .setNegativeButton(R.string.no, null)
        .show()
    }

    binding.videoPosition.setText(
      getString(
        R.string.proj_current_video_position,
        videoPosition.getFormattedTime(),
      )
    )
    binding.videoPosition.setOnClickListener {
      ClipboardUtils.copyText(videoPosition.getFormattedTime())
    }

    binding.subtitlePreview.setSubtitle(previewSubtitle)
    binding.subtitlePreview.setVideoPosition(0L)

    setTextWatchers()
    binding.tieStartTime.setText(unmodifiedParagraph.startTime.formattedTime)
    binding.tieEndTime.setText(unmodifiedParagraph.endTime.formattedTime)
    binding.tieText.setText(unmodifiedParagraph.text)

    binding.dialogButtons.cancel.setOnClickListener { dismiss() }
    binding.dialogButtons.save.setOnClickListener { saveParagraph() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun setTextWatchers() {
    binding.tieStartTime.afterTextChanged { s ->
      when (
        val startTimeResult = paragraphValidator.checkTime(s.toString().trim())
      ) {
        is ValidationResult.Success ->
          binding.tilStartTime.isErrorEnabled = false
        is ValidationResult.Error ->
          binding.tilStartTime.setError(startTimeResult.message)
      }
      updateSaveButton()
    }
    binding.tieEndTime.afterTextChanged { s ->
      when (
        val endTimeResult = paragraphValidator.checkTime(s.toString().trim())
      ) {
        is ValidationResult.Success -> binding.tilEndTime.isErrorEnabled = false
        is ValidationResult.Error ->
          binding.tilEndTime.setError(endTimeResult.message)
      }
      updateSaveButton()
    }
    binding.tieText.afterTextChanged { s ->
      when (
        val textResult = paragraphValidator.checkText(s.toString().trim())
      ) {
        is ValidationResult.Success -> binding.tilText.isErrorEnabled = false
        is ValidationResult.Error ->
          binding.tilText.setError(textResult.message)
      }
      previewSubtitle.paragraphs[0].text = s.toString().trim()
      binding.subtitlePreview.invalidate()
      updateSaveButton()
    }
  }

  /**
   * Adds a new paragraph or sets new values for an index of an existing
   * paragraph.
   */
  private fun saveParagraph() {
    val startTime =
      binding.tieStartTime.text.toString().trim().getMilliseconds()
    val endTime = binding.tieEndTime.text.toString().trim().getMilliseconds()
    val text = binding.tieText.text.toString().trim()

    if (isExistingParagraph) {
      subtitlesViewModel.setParagraph(
        index = paragraphIndex,
        startTime = startTime,
        endTime = endTime,
        text = text,
      )
    } else {
      subtitlesViewModel.addParagraph(
        index = getIndexForNewParagraph(),
        startTime = startTime,
        endTime = endTime,
        text = text,
      )
    }
    dismiss()
  }

  /**
   * Returns an index for a new paragraph to be added, it goes through the list
   * of paragraphs and if the position of the video is greater than the start
   * time of the paragraph it increments this value to the index, otherwise it
   * returns index 0.
   *
   * @return Index 0 or the index of the last paragraph that the video position
   *   is greater than the start time.
   */
  private fun getIndexForNewParagraph(): Int {
    val paragraphs = subtitlesViewModel.paragraphs
    var index = 0
    for (i in paragraphs.indices) {
      val paragraph = paragraphs[i]
      if (videoPosition >= paragraph.startTime.milliseconds) {
        index = i + 1
      }
    }

    return index
  }

  /**
   * Updates the state of the save button, if there are no errors in the fields
   * and the paragraph has been modified the button is enabled, otherwise it is
   * disabled.
   */
  private fun updateSaveButton() {
    val isModified =
      if (isExistingParagraph) {
        paragraphValidator.isModified(
          unmodifiedParagraph,
          binding.tieStartTime.text.toString().trim(),
          binding.tieEndTime.text.toString().trim(),
          binding.tieText.text.toString().trim(),
        )
      } else true // Always true if the user is adding a new paragraph.

    binding.dialogButtons.save.isEnabled =
      isModified &&
        binding.tilStartTime.isErrorEnabled.not() &&
        binding.tilEndTime.isErrorEnabled.not() &&
        binding.tilText.isErrorEnabled.not()
  }
}
