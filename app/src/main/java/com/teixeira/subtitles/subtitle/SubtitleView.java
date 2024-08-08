package com.teixeira.subtitles.subtitle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.teixeira.subtitles.models.Subtitle;
import java.util.Objects;

public class SubtitleView extends View {

  private TextPaint textPaint;
  private Subtitle subtitle;

  public SubtitleView(Context context) {
    this(context, null);
  }

  public SubtitleView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    textPaint = new TextPaint();
    textPaint.setAntiAlias(true);
    textPaint.setTextSize(35);
    textPaint.setColor(Color.WHITE);
  }

  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);

    drawText(canvas);
  }

  public void setSubtitle(@Nullable Subtitle subtitle) {
    this.subtitle = subtitle;
    invalidate();
  }

  private void drawText(Canvas canvas) {

    if (subtitle == null) {
      return;
    }

    Spanned text;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      text = Html.fromHtml(subtitle.getText(), Html.FROM_HTML_MODE_LEGACY);
    } else {
      text = Html.fromHtml(subtitle.getText());
    }

    SpannableString span = new SpannableString(text);
    span.setSpan(
        new BackgroundColorSpan(Color.BLACK), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

    StaticLayout staticLayout =
        StaticLayout.Builder.obtain(span, 0, span.length(), textPaint, canvas.getWidth())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build();

    canvas.save();
    canvas.translate(0, (getHeight() - 60) - staticLayout.getHeight());
    staticLayout.draw(canvas);
    canvas.restore();
  }
}
