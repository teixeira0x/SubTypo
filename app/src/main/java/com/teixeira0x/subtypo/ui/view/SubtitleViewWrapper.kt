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
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.media3.common.text.Cue.Builder
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import com.teixeira0x.subtypo.domain.model.Cue

class SubtitleViewWrapper
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

  private val subtitleView = SubtitleView(context)

  init {
    subtitleView.setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
    subtitleView.setStyle(
      CaptionStyleCompat(
        Color.WHITE,
        Color.BLACK,
        Color.TRANSPARENT,
        CaptionStyleCompat.EDGE_TYPE_NONE,
        Color.WHITE,
        Typeface.createFromAsset(context.assets, "fonts/roboto_regular.ttf"),
      )
    )
    addView(
      subtitleView,
      LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
    )
  }

  /**
   * Set the cue list, filter with the given time and convert to the
   * SubtitleView cue list.
   *
   * @param cues The list of cues, SubTypo Domain Cue.
   * @param videoPosition The position of the video to filter the cues.
   */
  fun setCues(cues: List<Cue>, videoPosition: Long) {
    if (cues.isEmpty()) {
      subtitleView.setCues(emptyList())
      return
    }

    val filteredCues =
      cues.filter {
        it.startTime <= videoPosition && it.endTime >= videoPosition
      }

    subtitleView.setCues(filteredCues.map { it.toViewCue() })
  }

  private fun Cue.toViewCue(): androidx.media3.common.text.Cue {
    return Builder().setText(text).build()
  }
}
