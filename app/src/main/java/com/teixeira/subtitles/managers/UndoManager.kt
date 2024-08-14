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

package com.teixeira.subtitles.managers

import com.teixeira.subtitles.subtitle.models.TimedTextObject

/**
 * This class manages the undo and redo stack.
 *
 * @author Felipe Teixeira
 */
class UndoManager(var maxStackSize: Int) {

  var isEnabled = true

  fun pushStack(timedTextObject: TimedTextObject) {}

  fun canUndo() = false

  fun canRedo() = false
}
