package com.teixeira.subtitles.utils;

import android.view.View;
import android.widget.ImageView;

public class UiUtils {

  public static void setImageEnabled(ImageView imageView, boolean enabled) {
    imageView.animate().alpha(enabled ? 1.0f : 0.5f).start();
    imageView.setClickable(enabled);
  }

  public static void rotateView(View view, float rotation) {
    view.animate().rotation(rotation).start();
  }
}
