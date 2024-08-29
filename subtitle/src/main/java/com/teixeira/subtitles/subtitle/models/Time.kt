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

import android.os.Parcelable
import com.teixeira.subtitles.subtitle.utils.TimeUtils
import kotlinx.parcelize.Parcelize

/** @author Felipe Teixeira */
@Parcelize
data class Time(var milliseconds: Long) : Parcelable {

  val time: String
    get() = TimeUtils.getTime(this.milliseconds)

  internal fun getState(): TimeState {
    return TimeState(milliseconds)
  }

  internal fun restoreState(state: TimeState) {
    this.milliseconds = state.milliseconds
  }
}

data class TimeState(val milliseconds: Long)
