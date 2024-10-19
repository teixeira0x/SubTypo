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

package com.teixeira.subtitles.ui.subtitle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.teixeira.subtitles.subtitle.models.Paragraph;
import com.teixeira.subtitles.subtitle.models.Subtitle;
import com.teixeira.subtitles.subtitle.utils.SubtitleUtils;
import java.util.List;

/**
 * @author Felipe Teixeira
 */
public class SubtitleView extends View {

  public static final float DEFAULT_BOTTOM_PADDING_FRACTION = 0.08f;

  private final SubtitlePainter painter;
  private List<Paragraph> paragraphs;
  private Subtitle subtitle;

  private long videoPosition;

  public SubtitleView(Context context) {
    this(context, null);
  }

  public SubtitleView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    this.painter = new SubtitlePainter(context);
  }

  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);

    if (subtitle == null || paragraphs == null) {
      return;
    }

    int left = getPaddingLeft();
    int top = getPaddingTop();
    int right = getWidth() - getPaddingRight();
    int bottom = getHeight() - getPaddingBottom();

    for (Paragraph paragraph : paragraphs) {
      painter.draw(
          canvas, subtitle.getSubtitleFormat(), paragraph, Color.BLACK, left, top, right, bottom);
    }
  }

  public void setSubtitle(@Nullable Subtitle subtitle) {
    this.subtitle = subtitle;
    setVideoPosition(videoPosition);
  }

  public void setVideoPosition(long videoPosition) {
    paragraphs = null;
    if (subtitle != null) {
      paragraphs = SubtitleUtils.findParagraphsAt(subtitle.getParagraphs(), videoPosition);
    }
    this.videoPosition = videoPosition;
    invalidate();
  }
}
