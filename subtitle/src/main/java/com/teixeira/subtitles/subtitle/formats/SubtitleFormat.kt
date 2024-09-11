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

import android.text.SpannableStringBuilder
import com.teixeira.subtitles.subtitle.models.Paragraph
import com.teixeira.subtitles.subtitle.models.SyntaxError

/** @author Felipe Teixeira */
abstract class SubtitleFormat protected constructor(val name: String, val extension: String) {

  class Builder private constructor(val extension: String) {

    companion object {
      @JvmStatic val availableExtensions = arrayOf(".srt")

      @JvmStatic val defaultBuilder = Builder.from(".srt")

      @JvmStatic
      fun from(extension: String): Builder {
        require(availableExtensions.contains(extension)) {
          "The extension: $extension is not in available extensions"
        }
        return Builder(extension)
      }
    }

    private var frameRate: Float? = null

    fun setFrameRate(frameRate: Float) = apply { this.frameRate = frameRate }

    fun build(): SubtitleFormat {
      return when (extension) {
        ".srt" -> SubRipFormat()
        else -> throw IllegalArgumentException("Invalid format extension")
      }
    }
  }

  private val _errorList = ArrayList<SyntaxError>()

  val errorList: List<SyntaxError>
    get() = _errorList

  /**
   * Convert paragraph list to text in caption format.
   *
   * @param paragraphs The list of paragraphs to convert.
   * @return Converted text.
   */
  abstract fun toText(paragraphs: List<Paragraph>): String

  /**
   * Analyze the obtained text and recover the paragraphs.
   *
   * @param text Text to analyze.
   * @return List of recovered paragraphs.
   */
  abstract fun parseText(text: String): MutableList<Paragraph>

  /**
   * Generate span based on caption format for given text.
   *
   * @param text Paragraph text to generate spans.
   * @return Spannable text.
   */
  abstract fun generateSpan(text: String): SpannableStringBuilder

  protected fun addError(error: SyntaxError) {
    _errorList.add(error)
  }
}
