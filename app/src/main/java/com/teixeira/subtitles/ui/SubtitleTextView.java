package com.teixeira.subtitles.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.VideoView;
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

    setBackgroundColor(Color.BLACK);
    setTextColor(Color.WHITE);
    setGravity(Gravity.CENTER);
  }

  public void setSubtitle(Subtitle subtitle) {
    Objects.requireNonNull(subtitle);

    setText(subtitle.getText());
  }
}
