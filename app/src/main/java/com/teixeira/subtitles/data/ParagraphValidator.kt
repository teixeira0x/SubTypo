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

package com.teixeira.subtitles.data

import android.content.Context
import com.teixeira.subtitles.R
import com.teixeira.subtitles.subtitle.models.Paragraph
import com.teixeira.subtitles.subtitle.utils.TimeUtils

/**
 * Class to validate fields in a paragraph.
 *
 * @author Felipe Teixeira
 */
class ParagraphValidator(val context: Context) {

  fun checkTime(formattedTime: String): ValidationResult {
    return if (TimeUtils.isValidTime(formattedTime.split(":"))) {
      ValidationResult.Success
    } else ValidationResult.Error(context.getString(R.string.subtitle_paragraph_invalid_time))
  }

  fun checkText(text: String): ValidationResult {
    return if (text.lines().firstOrNull { it.isEmpty() || it.isBlank() } == null) {
      ValidationResult.Success
    } else ValidationResult.Error(context.getString(R.string.warn_text_field_cannot_empty))
  }

  fun isModified(
    unmodifiedParagraph: Paragraph,
    startTime: String,
    endTime: String,
    text: String,
  ): Boolean {
    return unmodifiedParagraph.startTime.formattedTime != startTime ||
      unmodifiedParagraph.endTime.formattedTime != endTime ||
      unmodifiedParagraph.text != text
  }
}
