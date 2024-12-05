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

package com.teixeira0x.subtypo.subtitle.formats

import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import com.teixeira0x.subtypo.subtitle.models.Paragraph
import com.teixeira0x.subtypo.subtitle.models.SyntaxError
import com.teixeira0x.subtypo.subtitle.models.Time
import com.teixeira0x.subtypo.subtitle.utils.TimeUtils
import com.teixeira0x.subtypo.subtitle.utils.TimeUtils.getMilliseconds

/** @author Felipe Teixeira */
class SubRipFormat : SubtitleFormat("SubRip", ".srt") {

  override fun toText(paragraphs: List<Paragraph>): String {
    val sb = StringBuilder()
    for (i in paragraphs.indices) {
      val paragraph = paragraphs[i]
      sb
        .append(i + 1)
        .append("\n")
        .append(paragraph.startTime.formattedTime)
        .append(" --> ")
        .append(paragraph.endTime.formattedTime)
        .append("\n")
        .append(paragraph.text)
        .append("\n\n")
    }
    return sb.toString().trim()
  }

  override fun parseText(text: String): MutableList<Paragraph> {
    val paragraphs = mutableListOf<Paragraph>()
    val lines = text.lines()

    var captionNumber = 0
    var lineIndex = 0

    while (lineIndex < lines.size) {
      var line = lines[lineIndex].trim()
      if (line.isEmpty()) {
        lineIndex++
        continue
      }

      val expectedNumber = captionNumber + 1
      val number = parseCaptionNumber(expectedNumber, lineIndex, line)

      lineIndex++
      val times =
        if (lineIndex < lines.size) parseTimeCode(lineIndex, lines[lineIndex])
        else {
          addError(SyntaxError("Unexpected end of file", lineIndex))
          null
        }

      if (number == expectedNumber && times != null) {
        val (startTime, endTime) = times

        lineIndex++
        val textBuilder = StringBuilder()
        while (lineIndex < lines.size && lines[lineIndex].trim().isNotEmpty()) {
          textBuilder.append(lines[lineIndex].trim()).append("\n")
          lineIndex++
        }

        paragraphs.add(
          Paragraph(
            Time(startTime.getMilliseconds()),
            Time(endTime.getMilliseconds()),
            textBuilder.toString().trim(),
          )
        )

        captionNumber++
        lineIndex++
      }
    }

    return paragraphs
  }

  /**
   * Convert the given line to a number and check if the number is as expected.
   *
   * @param expectedNumber The expected number.
   * @return The line number.
   */
  private fun parseCaptionNumber(expectedNumber: Int, lineIndex: Int, line: String): Int {
    val number = line.toIntOrNull() ?: -1
    if (expectedNumber != number) {
      addError(SyntaxError("Found number: $number, expected number: $expectedNumber", lineIndex))
    }
    return number
  }

  /**
   * Parses the given line of timing code and returns the timings if it is valid code.
   *
   * @param timeCodeLine Timeline of code to analyze.
   * @return The start time and end time if the timecode is valid.
   */
  private fun parseTimeCode(lineIndex: Int, timeCodeLine: String): Array<String>? {
    val timeCodes = timeCodeLine.split(" --> ")
    if (timeCodes.size != 2) {
      addError(SyntaxError("Invalid time code format", lineIndex))
      return null
    }

    val (startTime, endTime) = timeCodes

    return if (
      TimeUtils.isValidTime(startTime.split(":")) || TimeUtils.isValidTime(endTime.split(":"))
    ) {
      arrayOf(startTime, endTime)
    } else {
      addError(SyntaxError("Invalid time code", lineIndex))
      null
    }
  }

  /**
   * Generate span based on caption format for given text.
   *
   * @param text Paragraph text to generate spans.
   * @return Spannable text.
   */
  override fun generateSpan(text: String): SpannableStringBuilder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      Html.fromHtml(text.replace("\n", "<br/>"), Html.FROM_HTML_MODE_LEGACY)
    } else {
      Html.fromHtml(text.replace("\n", "<br/>"))
    }
      as SpannableStringBuilder
  }
}
