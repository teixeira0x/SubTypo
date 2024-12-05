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

package com.teixeira0x.subtypo.subtitle.models

import com.teixeira0x.subtypo.subtitle.formats.SubtitleFormat
import java.util.Collections

/** @author Felipe Teixeira */
data class Subtitle(
  var name: String = "undefined",
  var subtitleFormat: SubtitleFormat = SubtitleFormat.Builder.defaultBuilder.build(),
  val paragraphs: MutableList<Paragraph> = ArrayList<Paragraph>(),
) {

  companion object {
    const val MAX_FILE_SIZE = 1024 * 1024 * 20 // 20 MB
    const val MAX_STATES_SIZE = 20
  }

  val stateManager = SubtitleStateManager(this)

  val fullName: String
    get() = name + subtitleFormat.extension

  fun swapParagraph(first: Int, second: Int) {
    Collections.swap(paragraphs, first, second)
    stateManager.pushState()
  }

  fun addParagraph(index: Int = paragraphs.size, paragraph: Paragraph) {
    paragraphs.add(index, paragraph)
    stateManager.pushState()
  }

  fun setParagraph(index: Int, paragraph: Paragraph) {
    paragraphs.set(index, paragraph)
    stateManager.pushState()
  }

  fun removeParagraph(index: Int) {
    paragraphs.removeAt(index)
    stateManager.pushState()
  }

  fun canUndo(): Boolean = stateManager.canUndo()

  fun canRedo(): Boolean = stateManager.canRedo()

  fun undo() {
    stateManager.undo()
  }

  fun redo() {
    stateManager.redo()
  }

  fun toText(): String {
    return subtitleFormat.toText(paragraphs)
  }
}
