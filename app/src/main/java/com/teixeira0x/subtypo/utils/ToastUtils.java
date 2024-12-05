package com.teixeira0x.subtypo.utils;

import android.widget.Toast;
import com.teixeira0x.subtypo.App;

public class ToastUtils {

  public static void showShort(int message, Object... args) {
    showShort(App.getInstance().getString(message, args));
  }

  public static void showShort(String message) {
    Toast.makeText(App.getInstance(), message, Toast.LENGTH_SHORT).show();
  }

  public static void showLong(int message, Object... args) {
    showLong(App.getInstance().getString(message, args));
  }

  public static void showLong(String message) {
    Toast.makeText(App.getInstance(), message, Toast.LENGTH_LONG).show();
  }
}
