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

package com.teixeira0x.subtypo.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.teixeira0x.subtypo.domain.model.Cue

class SubtitleView
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

  companion object {
    const val DEFAULT_BOTTOM_PADDING_FRACTION = 0.08f
  }

  private val painter = SubtitlePainter(context)
  private var cues: List<Cue>? = null

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val cues = cues ?: return
    val left = paddingLeft
    val top = paddingTop
    val right = width - paddingRight
    val bottom = height - paddingBottom

    for (cue in cues) {
      painter.draw(canvas, cue, Color.BLACK, left, top, right, bottom)
    }
  }

  fun setCues(cues: List<Cue>, videoPosition: Long) {
    this.cues =
      cues.filter {
        it.startTime <= videoPosition && it.endTime >= videoPosition
      }
    invalidate()
  }
}
