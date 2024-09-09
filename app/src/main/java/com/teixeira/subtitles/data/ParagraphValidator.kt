package com.teixeira.subtitles.data

import android.content.Context
import com.teixeira.subtitles.R
import com.teixeira.subtitles.subtitle.utils.TimeUtils

class ParagraphValidator(val context: Context) {

  fun checkTime(time: String): ValidationResult {
    return if (TimeUtils.isValidTime(time.split(":"))) {
      ValidationResult.Success
    } else ValidationResult.Error(context.getString(R.string.subtitle_paragraph_invalid_time))
  }

  fun checkText(text: String): ValidationResult {
    return if (text.lines().firstOrNull { it.isEmpty() || it.isBlank() } == null) {
      ValidationResult.Success
    } else ValidationResult.Error(context.getString(R.string.warn_text_field_cannot_empty))
  }
}
