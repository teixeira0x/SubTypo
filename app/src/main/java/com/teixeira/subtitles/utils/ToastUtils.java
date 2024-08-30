package com.teixeira.subtitles.utils;

import android.widget.Toast;
import com.teixeira.subtitles.App;

public class ToastUtils {

  public static void showShort(int message, Object... args) {
    showShort(App.Companion.getInstance().getString(message, args));
  }

  public static void showShort(String message) {
    Toast.makeText(App.Companion.getInstance(), message, Toast.LENGTH_SHORT).show();
  }

  public static void showLong(int message, Object... args) {
    showLong(App.Companion.getInstance().getString(message, args));
  }

  public static void showLong(String message) {
    Toast.makeText(App.Companion.getInstance(), message, Toast.LENGTH_LONG).show();
  }
}
