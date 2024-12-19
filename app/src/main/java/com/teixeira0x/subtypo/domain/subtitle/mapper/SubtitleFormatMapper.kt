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

package com.teixeira0x.subtypo.domain.subtitle.mapper

import com.teixeira0x.subtypo.domain.subtitle.format.SRTFormat
import com.teixeira0x.subtypo.domain.subtitle.format.SubtitleFormat

object SubtitleFormatMapper {
  private val formatByValues = mapOf(1 to SRTFormat)

  fun Int.toSubtitleFormat(): SubtitleFormat {
    return formatByValues[this]
      ?: throw IllegalArgumentException("Invalid format value: $this")
  }

  fun String.toSubtitleFormat(): SubtitleFormat {
    return formatByValues.values.find { it.extension == this }
      ?: throw IllegalArgumentException("Unknown format extension: $this")
  }

  fun SubtitleFormat.toValue(): Int {
    return formatByValues.entries.find { it.value == this }?.key
      ?: throw IllegalArgumentException("Unknown format: ${this.extension}")
  }
}
