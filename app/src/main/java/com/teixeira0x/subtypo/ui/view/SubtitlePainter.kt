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
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import android.util.DisplayMetrics
import com.teixeira0x.subtypo.domain.model.Cue
import kotlin.math.ceil
import kotlin.math.max

internal class SubtitlePainter(private val context: Context) {

  private val textPaint = TextPaint()
  private val textSize =
    (13f * context.resources.displayMetrics.densityDpi) /
      DisplayMetrics.DENSITY_DEFAULT
  private val font =
    Typeface.createFromAsset(context.assets, "fonts/roboto_regular.ttf")

  private var textLayout: StaticLayout? = null
  private var text: SpannableStringBuilder? = null

  init {
    textPaint.setAntiAlias(true)
    textPaint.setSubpixelText(true)
  }

  fun draw(
    canvas: Canvas,
    cue: Cue,
    backgroundColor: Int,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
  ) {
    val text = cue.text

    if (this.text?.equals(text) ?: false) {
      return drawText(canvas, backgroundColor, left, top, right, bottom)
    }
    this.text = SpannableStringBuilder(text)
    drawText(canvas, backgroundColor, left, top, right, bottom)
  }

  private fun drawText(
    canvas: Canvas,
    backgroundColor: Int,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
  ) {
    val parentWidth = right - left
    val parentHeight = bottom - top

    val text = text ?: return

    textPaint.setTextSize(textSize)
    textPaint.setTypeface(font)

    if (Color.alpha(backgroundColor) > 0) {
      text.setSpan(
        BackgroundColorSpan(backgroundColor),
        0,
        text.length,
        Spanned.SPAN_PRIORITY,
      )
    }

    textLayout =
      StaticLayout.Builder.obtain(text, 0, text.length, textPaint, parentWidth)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .build()

    var textWidth = 0
    val textHeight = textLayout!!.height

    val lineCount = textLayout!!.lineCount
    for (i in 0 until lineCount) {
      textWidth = max(ceil(textLayout!!.getLineWidth(i)).toInt(), textWidth)
    }

    textLayout =
      StaticLayout.Builder.obtain(text, 0, text.length, textPaint, textWidth)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .build()

    val textLeft = (parentWidth - textWidth) / 2 + left
    val textTop =
      bottom -
        textHeight -
        (parentHeight * SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION)
    val textRight = textLeft + textWidth
    val textBottom = textTop + textHeight

    val count = canvas.save()
    canvas.translate(textLeft.toFloat(), textTop.toFloat())

    textPaint.setColor(Color.WHITE)
    textPaint.setStyle(Paint.Style.FILL)
    textLayout!!.draw(canvas)

    canvas.restoreToCount(count)
  }
}
