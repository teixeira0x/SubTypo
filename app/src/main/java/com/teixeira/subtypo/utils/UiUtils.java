package com.teixeira.subtypo.utils;

import android.view.View;

public class UiUtils {

  public static void rotateView(View view, float rotation) {
    view.animate().rotation(rotation).start();
  }
}
