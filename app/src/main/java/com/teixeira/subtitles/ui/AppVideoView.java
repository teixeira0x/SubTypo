package com.teixeira.subtitles.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class AppVideoView extends VideoView {

  public AppVideoView(Context context) {
    this(context, null);
  }

  public AppVideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AppVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
}