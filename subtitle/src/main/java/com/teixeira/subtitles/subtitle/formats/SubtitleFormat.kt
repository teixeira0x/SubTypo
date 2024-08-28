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

import com.teixeira.subtitles.subtitle.models.Paragraph
import com.teixeira.subtitles.subtitle.models.SyntaxError

/** @author Felipe Teixeira */
abstract class SubtitleFormat(val name: String, val extension: String) {

  companion object {

    @JvmStatic
    fun getExtensionFormat(extension: String): SubtitleFormat {
      return when (extension) {
        ".srt" -> SubRipFormat()
        else -> SubRipFormat()
      }
    }
  }

  private val _errorList = ArrayList<SyntaxError>()

  val errorList: List<SyntaxError>
    get() = _errorList

  abstract fun toText(paragraphs: List<Paragraph>): String

  abstract fun parseText(text: String): MutableList<Paragraph>

  protected fun addError(error: SyntaxError) {
    _errorList.add(error)
  }
}
