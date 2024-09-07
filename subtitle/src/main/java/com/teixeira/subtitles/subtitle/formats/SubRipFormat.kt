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

package com.teixeira.subtitles.subtitle.formats

import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import com.teixeira.subtitles.subtitle.models.Paragraph
import com.teixeira.subtitles.subtitle.models.SyntaxError
import com.teixeira.subtitles.subtitle.models.Time
import com.teixeira.subtitles.subtitle.utils.TimeUtils

/** @author Felipe Teixeira */
class SubRipFormat : SubtitleFormat("SubRip", ".srt") {

  override fun toText(paragraphs: List<Paragraph>): String {
    val sb = StringBuilder()
    for (i in paragraphs.indices) {
      val paragraph = paragraphs[i]
      sb
        .append(i + 1)
        .append("\n")
        .append(paragraph.startTime.time)
        .append(" --> ")
        .append(paragraph.endTime.time)
        .append("\n")
        .append(paragraph.text)
        .append("\n\n")
    }
    return sb.toString().trim()
  }

  override fun parseText(text: String): MutableList<Paragraph> {
    val paragraphs = mutableListOf<Paragraph>()
    val lines = text.split("\n")

    var captionNumber = 0
    var lineIndex = 0

    while (lineIndex < lines.size) {
      var line = lines[lineIndex].trim()
      if (line.isEmpty()) {
        lineIndex++
        continue
      }

      if (line.all { it.isDigit() }) {
        val number = line.toIntOrNull()
        if (number == null || captionNumber + 1 != number) {
          addError(
            SyntaxError("Found number: $number, expected number: ${captionNumber + 1}", lineIndex)
          )
          lineIndex++
          continue
        }

        lineIndex++
        if (lineIndex >= lines.size) {
          addError(SyntaxError("Unexpected end of file after caption number", lineIndex))
          break
        }

        val timeCodesLine = lines[lineIndex].trim()
        if (timeCodesLine.isEmpty()) {
          addError(SyntaxError("Time codes line not found", lineIndex))
          continue
        }

        val times = timeCodesLine.split(" --> ")
        if (times.size != 2) {
          addError(SyntaxError("Invalid time code format", lineIndex))
          continue
        }

        val startTime = times[0].trim()
        val endTime = times[1].trim()

        if (
          !TimeUtils.isValidTime(startTime.split(":").toTypedArray()) ||
            !TimeUtils.isValidTime(endTime.split(":").toTypedArray())
        ) {
          addError(SyntaxError("Incorrect time formatting", lineIndex))
          continue
        }

        lineIndex++
        val textBuilder = StringBuilder()
        while (lineIndex < lines.size && lines[lineIndex].trim().isNotEmpty()) {
          textBuilder.append(lines[lineIndex].trim()).append("\n")
          lineIndex++
        }

        paragraphs.add(
          Paragraph(
            Time(TimeUtils.getMilliseconds(startTime)),
            Time(TimeUtils.getMilliseconds(endTime)),
            textBuilder.toString().trim(),
          )
        )

        captionNumber++
        lineIndex++
      } else {
        addError(SyntaxError("Caption number not found", lineIndex))
        break
      }
    }

    return paragraphs
  }

  override fun generateSpan(text: String): SpannableStringBuilder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      Html.fromHtml(text.replace("\n", "<br/>"), Html.FROM_HTML_MODE_LEGACY)
    } else {
      Html.fromHtml(text.replace("\n", "<br/>"))
    }
      as SpannableStringBuilder
  }
}
