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

/** @author Felipe Teixeira */
class SubtitleStateManager internal constructor(private val subtitle: Subtitle) {
  private val savedStates = ArrayList<StateItem>()
  private var state = 0

  init {
    pushState()
  }

  internal fun pushState() {
    while (state < savedStates.size - 1) {
      savedStates.removeAt(savedStates.size - 1)
    }

    val paragraphStates = subtitle.paragraphs.map { it.getState() }
    savedStates.add(StateItem(paragraphStates))
    if (savedStates.size > 1) {
      state++
    }

    while (state > 1 && savedStates.size > Subtitle.MAX_STATES_SIZE) {
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
    val paragraphs = subtitle.paragraphs
    val paragraphStates = state.paragraphStates

    while (paragraphs.size > paragraphStates.size) {
      paragraphs.removeAt(0)
    }

    for (i in paragraphStates.indices) {
      val paragraphState = paragraphStates[i]
      if (i >= paragraphs.size) {
        paragraphs.add(paragraphState.getParagraph())
        continue
      }

      paragraphs[i].restoreState(paragraphState)
    }
  }

  data class StateItem(val paragraphStates: List<ParagraphState>)
}
