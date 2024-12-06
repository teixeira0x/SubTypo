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

package com.teixeira0x.subtypo.ui.subtitle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.DisplayMetrics;
import com.teixeira0x.subtypo.subtitle.formats.SubtitleFormat;
import com.teixeira0x.subtypo.subtitle.models.Paragraph;

/**
 * Paints subtitle.
 *
 * @see SubtitlePainter.java
 *     https://github.com/androidx/media/blob/c35a9d62baec57118ea898e271ac66819399649b/libraries/ui/src/main/java/androidx/media3/ui/SubtitlePainter.java
 */
final class SubtitlePainter {

  private final TextPaint textPaint;

  private int parentLeft, parentTop, parentRight, parentBottom;
  private int backgroundColor;

  private float textSize;
  private Typeface font;

  private SpannableStringBuilder text;
  private StaticLayout textLayout;

  public SubtitlePainter(Context context) {
    Resources resources = context.getResources();
    DisplayMetrics displayMetrics = resources.getDisplayMetrics();
    textSize = (13f * displayMetrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    font = Typeface.createFromAsset(context.getAssets(), "fonts/roboto_regular.ttf");

    textPaint = new TextPaint();
    textPaint.setAntiAlias(true);
    textPaint.setSubpixelText(true);
  }

  public void draw(
      Canvas canvas,
      SubtitleFormat subtitleFormat,
      Paragraph paragraph,
      int backgroundColor,
      int left,
      int top,
      int right,
      int bottom) {
    this.backgroundColor = backgroundColor;
    this.parentLeft = left;
    this.parentTop = top;
    this.parentRight = right;
    this.parentBottom = bottom;

    String text = paragraph.getText();
    if (TextUtils.isEmpty(text)) {
      return;
    }

    this.text = new SpannableStringBuilder(text);
    drawTextLayout(canvas);
  }

  private void drawTextLayout(Canvas canvas) {
    int parentWidth = parentRight - parentLeft;
    int parentHeight = parentBottom - parentTop;

    textPaint.setTextSize(textSize);
    textPaint.setTypeface(font);

    if (Color.alpha(backgroundColor) > 0) {
      text.setSpan(
          new BackgroundColorSpan(backgroundColor), 0, text.length(), Spanned.SPAN_PRIORITY);
    }

    textLayout =
        StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, parentWidth)
            .setAlignment(StaticLayout.Alignment.ALIGN_CENTER)
            .build();

    int textWidth = 0;
    int textHeight = textLayout.getHeight();

    int lineCount = textLayout.getLineCount();
    for (int i = 0; i < lineCount; i++) {
      textWidth = Math.max((int) Math.ceil(textLayout.getLineWidth(i)), textWidth);
    }

    textLayout =
        StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, textWidth)
            .setAlignment(StaticLayout.Alignment.ALIGN_CENTER)
            .build();

    int textLeft = (parentWidth - textWidth) / 2 + parentLeft;
    int textTop =
        parentBottom
            - textHeight
            - (int) (parentHeight * SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION);
    int textRight = textLeft + textWidth;
    int textBottom = textTop + textHeight;

    int count = canvas.save();
    canvas.translate(textLeft, textTop);

    textPaint.setColor(Color.WHITE);
    textPaint.setStyle(Paint.Style.FILL);
    textLayout.draw(canvas);

    canvas.restoreToCount(count);
  }
}
