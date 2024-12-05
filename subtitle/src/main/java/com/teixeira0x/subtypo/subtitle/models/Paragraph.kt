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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** @author Felipe Teixeira */
@Parcelize
data class Paragraph(var startTime: Time, var endTime: Time, var text: String) : Parcelable {

  fun areContentsEqual(other: Paragraph): Boolean {
    return startTime.milliseconds == other.startTime.milliseconds &&
      endTime.milliseconds == other.endTime.milliseconds &&
      text == other.text
  }

  internal fun getState(): ParagraphState {
    return ParagraphState(
      startTimeState = startTime.getState(),
      endTimeState = endTime.getState(),
      text = text,
    )
  }

  internal fun restoreState(state: ParagraphState) {
    this.startTime.restoreState(state.startTimeState)
    this.endTime.restoreState(state.endTimeState)
    this.text = state.text
  }
}

data class ParagraphState(
  val startTimeState: TimeState,
  val endTimeState: TimeState,
  val text: String,
) {

  fun getParagraph(): Paragraph {
    return Paragraph(
      startTime = startTimeState.getTime(),
      endTime = endTimeState.getTime(),
      text = text,
    )
  }
}
