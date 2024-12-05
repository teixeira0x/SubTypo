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

package com.teixeira0x.subtypo.subtitle.utils

import com.teixeira0x.subtypo.subtitle.models.Paragraph

/** @author Felipe Teixeira */
object SubtitleUtils {

  /**
   * Returns a list of paragraphs whose start and end times encompass the given millisecond value.
   *
   * @param milliseconds The time value, in milliseconds, that will be used to filter paragraphs.
   * @return A list of paragraphs whose time range (startTime to endTime) encompasses the given
   *   value. If no paragraphs meet the criteria, an empty list is returned.
   */
  @JvmStatic
  fun List<Paragraph>.findParagraphsAt(milliseconds: Long): List<Paragraph> {
    return filter {
      milliseconds >= it.startTime.milliseconds && milliseconds <= it.endTime.milliseconds
    }
  }
}
