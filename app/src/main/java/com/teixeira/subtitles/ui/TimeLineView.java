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

package com.teixeira.subtitles.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import com.google.android.material.color.MaterialColors;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.utils.VideoUtils;
import java.util.List;

/**
 * This class draws a timeline view.
 *
 * @author Felipe Teixeira
 * @since 2024-07-30
 */
public class TimeLineView extends View {

  private Rect bounds;
  private Paint paint;
  private boolean isTouching;
  private float zoom;
  private Scroller scroller;
  private float scrollX;

  private HandlerMotionListener handlerMotionListener;
  private int videoDuration;
  private int currentVideoPosition;
  private List<Subtitle> subtitles;

  public TimeLineView(Context context) {
    this(context, null);
  }

  public TimeLineView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TimeLineView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    bounds = new Rect();
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setTextSize(16);
    isTouching = false;
    zoom = 2.0f;
    scroller = new Scroller(context);
    scrollX = 0;

    videoDuration = 0;
    currentVideoPosition = 0;
    subtitles = null;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    drawSubtitles(canvas);
    drawTimeLine(canvas);
    drawCurrentVideoPositionHandler(canvas);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    /*float touchX = Math.max(0, Math.min(event.getX(), getWidth()));

    int newCurrentVideoPosition = (touchX / getWidth() * videoDuration) - zoom - scrollX;

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        isTouching = true;
        setCurrentVideoPosition(newCurrentVideoPosition);
        if (handlerMotionListener != null) {
          handlerMotionListener.handlerMotionListener(newCurrentVideoPosition);
          handlerMotionListener.onStartTouch();
        }
        break;

      case MotionEvent.ACTION_MOVE:
        if (isTouching) {
          setCurrentVideoPosition(newCurrentVideoPosition);
          if (handlerMotionListener != null) {
            handlerMotionListener.handlerMotionListener(newCurrentVideoPosition);
          }
        }
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        isTouching = false;
        if (handlerMotionListener != null) {
          handlerMotionListener.onStopTouch();
        }
        break;
    }*/

    return true;
  }

  @Override
  public void computeScroll() {
    super.computeScroll();
    if (scroller.computeScrollOffset()) {
      scrollX = scroller.getCurrX();
      invalidate();
    }
  }

  /**
   * Starts a scrolling animation from the given start position to the end position over a specified
   * duration.
   *
   * @param startX The starting horizontal position of the scroll.
   * @param endX The ending horizontal position of the scroll.
   * @param duration The duration of the scroll in milliseconds.
   */
  public void startScroll(float startX, float endX, int duration) {
    scroller.startScroll((int) startX, 0, (int) (endX - startX), 0, duration);
    invalidate();
  }

  /**
   * Sets the handler's motion listener to the specified listener.
   *
   * @param handlerMotionListener The new listener.
   */
  public void setHandlerMotionListener(HandlerMotionListener handlerMotionListener) {
    this.handlerMotionListener = handlerMotionListener;
  }

  /**
   * Sets the video duration to the new specified duration and redraws the view.
   *
   * @param duration The new duration.
   */
  public void setVideoDuration(int duration) {
    this.videoDuration = duration;
    invalidate();
  }

  /**
   * Sets the current position of the video to the specified current position and
   * redraws the view.
   *
   * @param currentVideoPosition New video current position.
   */
  public void setCurrentVideoPosition(int currentVideoPosition) {
    this.currentVideoPosition = currentVideoPosition;
    invalidate();
  }

  /**
   * Sets the list of subtitles to be drawn with the specified list and redraws the view.
   *
   * @param subtitles New list of captions to draw.
   */
  public void setSubtitles(List<Subtitle> subtitles) {
    this.subtitles = subtitles;
    invalidate();
  }

  /**
   * Draws lines for the seconds, minutes, and hours of the video.
   *
   * @param canvas The canvas to make the line drawings.
   */
  private void drawTimeLine(Canvas canvas) {

    int colorControlNormal =
        MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondaryVariant);
    paint.setColor(colorControlNormal);

    int width = canvas.getWidth();
    int height = canvas.getHeight();

    float centerX = width / 2;
    float centerY = height / 2;

    float x = 0;
    float y = 0;

    long seconds = videoDuration / 1000;
    for (int i = 0; i <= seconds; i++) {

      x = ((float) i / seconds * width) * zoom - scrollX;

      paint.setAlpha(100);

      if (i % 60 == 0) { // Minutes
        y = centerY + (centerY / 4);
      } else if (i % 3600 == 0) { // Hours
        y = centerY;
      } else {
        y = height - (height / 4);
        paint.setAlpha(90);
      }
      canvas.drawLine(x, y, x, height, paint);
    }
  }

  /**
   * Draws a simple manipulator for the current position of the video.
   *
   * @param canvas The screen for drawing the handler.
   */
  private void drawCurrentVideoPositionHandler(Canvas canvas) {

    int colorControlNormal =
        MaterialColors.getColor(this, com.google.android.material.R.attr.colorControlNormal);
    paint.setColor(colorControlNormal);

    int width = canvas.getWidth();
    int height = canvas.getHeight();

    float x = ((float) currentVideoPosition / videoDuration * width) * zoom - scrollX;
    canvas.drawLine(x, 0, x, height / 2, paint);

    float size = 8;
    Path path = new Path();
    path.moveTo(x, size);
    path.lineTo(x - size, 0);
    path.lineTo(x + size, 0);
    path.close();

    canvas.drawPath(path, paint);

    String currentVideoPositionText = VideoUtils.getTime(currentVideoPosition);
    paint.getTextBounds(currentVideoPositionText, 0, currentVideoPositionText.length(), bounds);
    canvas.drawText(currentVideoPositionText, x - (bounds.width() / 2), height / 2, paint);

    startScroll(scrollX, x, 200);
  }

  /**
   * Draws yellow rectangles for the subtitles.
   *
   * @param canvas The canvas for drawing the rectangles.
   */
  private void drawSubtitles(Canvas canvas) {

    if (subtitles == null) {
      return;
    }

    int width = canvas.getWidth();
    int height = canvas.getHeight();

    paint.setColor(Color.YELLOW);

    for (Subtitle subtitle : subtitles) {
      try {
        long startTime = VideoUtils.getMilliSeconds(subtitle.getStartTime());
        long endTime = VideoUtils.getMilliSeconds(subtitle.getEndTime());

        float left = ((float) startTime / videoDuration * width) * zoom - scrollX;
        float right = ((float) endTime / videoDuration * width) * zoom - scrollX;

        paint.setAlpha(subtitle.isInScreen() ? 90 : 80);

        canvas.drawRect(new RectF(left, 0, right, height), paint);
      } catch (Exception e) {
        // Ignore
      }
    }
  }

  /** Video current position handler motion listener */
  public interface HandlerMotionListener {

    /** This method is called when moving the current video position handler. */
    void handlerMotionListener(int position);

    /** This method is called when you touch the view. */
    void onStartTouch();

    /** This method is called when you stop touching the view. */
    void onStopTouch();
  }
}
