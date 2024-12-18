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

package com.teixeira0x.subtypo.domain.subtitle.format

import com.teixeira0x.subtypo.domain.model.Cue
import com.teixeira0x.subtypo.domain.subtitle.exception.SubtitleParseException
import com.teixeira0x.subtypo.utils.TimeUtils
import com.teixeira0x.subtypo.utils.TimeUtils.getFormattedTime

object SRTFormat : SubtitleFormat("SubRip", ".srt") {

  override fun toText(cues: List<Cue>): String {
    val sb = StringBuilder()
    for (i in cues.indices) {
      with(cues[i]) {
        sb
          .append(i + 1)
          .append("\n")
          .append(startTime.getFormattedTime())
          .append(" --> ")
          .append(endTime.getFormattedTime())
          .append("\n")
          .append(text)
          .append("\n\n")
      }
    }
    return sb.toString().trim()
  }

  @Throws(SubtitleParseException::class)
  override fun parseText(text: String): List<Cue> {
    val cues = mutableListOf<Cue>()
    val lines = text.lines()

    var index = 0
    var cueNum = 0

    while (index < lines.size) {
      val line = lines[index].trim()
      if (line.isEmpty()) {
        index++
        continue
      }

      cueNum++
      if (line.toIntOrNull() != cueNum) {
        throw SubtitleParseException(
          "Found number: ${line.toIntOrNull()}, expected number: $cueNum"
        )
      }

      index++
      val times = parseTimeCode(index, lines[index])

      index++
      val textBuilder = StringBuilder()
      while (index < lines.size && lines[index].trim().isNotEmpty()) {
        textBuilder.append(lines[index].trim()).append("\n")
        index++
      }

      cues.add(
        Cue(
          startTime = times[0].toLong(),
          endTime = times[1].toLong(),
          text = textBuilder.toString(),
        )
      )

      index++
    }

    return cues
  }

  @Throws(SubtitleParseException::class)
  private fun parseTimeCode(index: Int, timeCodeLine: String): List<String> {
    val timeCodes = timeCodeLine.split(" --> ")
    if (
      timeCodes.size != 2 ||
        !TimeUtils.isValidTime(timeCodes[0]) ||
        !TimeUtils.isValidTime(timeCodes[1])
    ) {
      throw SubtitleParseException(
        "Invalid time code: '$timeCodeLine' at line: $index"
      )
    }

    return timeCodes
  }
}
