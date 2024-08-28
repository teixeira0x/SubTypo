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

package com.teixeira.subtitles.subtitle.models

import com.teixeira.subtitles.subtitle.formats.SubRipFormat
import com.teixeira.subtitles.subtitle.formats.SubtitleFormat
import java.util.Collections

/** @author Felipe Teixeira */
data class Subtitle(
  var name: String = "undefined",
  var subtitleFormat: SubtitleFormat = SubRipFormat(),
  val paragraphs: MutableList<Paragraph> = ArrayList<Paragraph>(),
) {

  private val carataker = Caretaker()

  companion object {
    const val MAX_FILE_SIZE = 1024 * 1024 * 20 // 20 MB
    const val MAX_STATES_SIZE = 20
  }

  fun moveParagraph(targetIndex: Int) {
    if (targetIndex >= 0 && targetIndex < paragraphs.size) {
      var first: Int
      if (targetIndex - 1 >= 0) {
        first = targetIndex - 1
      } else if (targetIndex + 1 < paragraphs.size) {
        first = targetIndex + 1
      } else {
        first = targetIndex
      }
      swapParagraph(first, targetIndex)
    }
  }

  fun swapParagraph(first: Int, second: Int) {
    Collections.swap(paragraphs, first, second)
    carataker.pushState()
  }

  fun addParagraph(paragraph: Paragraph) {
    paragraphs.add(paragraph)
    carataker.pushState()
  }

  fun addParagraph(index: Int, paragraph: Paragraph) {
    paragraphs.add(index, paragraph)
    carataker.pushState()
  }

  fun setParagraph(index: Int, newParagraph: Paragraph) {
    paragraphs.set(index, newParagraph)
    carataker.pushState()
  }

  fun removeParagraph(paragraph: Paragraph) {
    paragraphs.remove(paragraph)
    carataker.pushState()
  }

  fun removeParagraph(index: Int) {
    paragraphs.removeAt(index)
    carataker.pushState()
  }

  fun canUndo(): Boolean = carataker.canUndo()

  fun canRedo(): Boolean = carataker.canRedo()

  fun undo() {
    carataker.undo()
  }

  fun redo() {
    carataker.redo()
  }

  fun toText(): String {
    return subtitleFormat.toText(paragraphs)
  }

  inner class Caretaker {
    private val savedStates = ArrayList<StateItem>()
    private var state = 0

    init {
      pushState()
    }

    fun pushState() {
      while (state < savedStates.size - 1) {
        savedStates.removeAt(savedStates.size - 1)
      }

      val paragraphStates = paragraphs.map { it.getState() }
      savedStates.add(StateItem(paragraphStates))
      if (savedStates.size > 1) {
        state++
      }

      while (state > 1 && savedStates.size > MAX_STATES_SIZE) {
        savedStates.removeAt(0)
        state--
      }
    }

    fun canUndo(): Boolean = state > 0

    fun canRedo(): Boolean = state < savedStates.size - 1

    fun undo() {
      if (canUndo()) {
        state--
        restoreState(savedStates[state])
      }
    }

    fun redo() {
      if (canRedo()) {
        state++
        restoreState(savedStates[state])
      }
    }

    fun restoreState(state: StateItem) {
      val paragraphStates = state.paragraphStates

      while (paragraphs.size > paragraphStates.size) {
        paragraphs.removeAt(0)
      }

      for (i in paragraphStates.indices) {
        val paragraphState = paragraphStates[i]
        if (i >= paragraphs.size) {
          paragraphs.add(paragraphState.toParagraph())
          continue
        }

        paragraphs[i].restoreState(paragraphState)
      }
    }

    fun ParagraphState.toParagraph(): Paragraph {
      return Paragraph(
        startTime = Time(startTimeState.milliseconds),
        endTime = Time(endTimeState.milliseconds),
        text = text,
      )
    }
  }

  inner class StateItem(val paragraphStates: List<ParagraphState>)
}
