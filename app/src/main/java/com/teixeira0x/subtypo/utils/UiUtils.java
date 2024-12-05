package com.teixeira0x.subtypo.utils;

import android.view.View;
import android.widget.ImageView;

public class UiUtils {

  public static void rotateView(View view, float rotation) {
    view.animate().rotation(rotation).start();
  }
}
