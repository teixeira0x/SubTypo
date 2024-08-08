package com.teixeira.subtitles.utils;

import android.widget.ImageView;

public class UiUtils {

  public static void setImageEnabled(ImageView imageView, boolean enabled) {
    imageView.animate().alpha(enabled ? 1.0f : 0.5f).start();
    imageView.setClickable(enabled);
  }
}
