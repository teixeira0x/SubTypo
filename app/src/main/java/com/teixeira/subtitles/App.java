package com.teixeira.subtitles;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class App extends Application {

  private static App sInstance;

  @Override
  public void onCreate() {
    sInstance = this;
    super.onCreate();

    DynamicColors.applyToActivitiesIfAvailable(this);
  }

  public static App getInstance() {
    return sInstance;
  }
}
