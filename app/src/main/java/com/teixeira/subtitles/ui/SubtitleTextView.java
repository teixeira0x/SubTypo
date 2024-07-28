package com.teixeira.subtitles.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.textview.MaterialTextView;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.models.Subtitle;
import java.util.Objects;

public class SubtitleTextView extends MaterialTextView {

  public SubtitleTextView(Context context) {
    this(context, null);
  }

  public SubtitleTextView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SubtitleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    setTypeface(ResourcesCompat.getFont(context, R.font.roboto_regular));
    setPadding(5, 5, 5, 5);
    setTextSize(15);

    // setBackgroundColor(Color.BLACK);
    setTextColor(Color.WHITE);
    setGravity(Gravity.CENTER);
  }

  public void setSubtitle(Subtitle subtitle) {
    Objects.requireNonNull(subtitle);

    Spanned text;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      // setText(Html.fromHtml(subtitle.getText(), Html.FROM_HTML_MODE_LEGACY));
      text = Html.fromHtml(subtitle.getText(), Html.FROM_HTML_MODE_LEGACY);
    } else {
      // setText(Html.fromHtml(subtitle.getText()));
      text = Html.fromHtml(subtitle.getText());
    }

    SpannableString span = new SpannableString(text);
    span.setSpan(
        new BackgroundColorSpan(Color.BLACK), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    setText(span);
  }
}
