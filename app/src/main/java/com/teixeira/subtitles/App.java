package com.teixeira.subtitles;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import com.google.android.material.color.DynamicColors;

public class App extends Application {

  private static App sInstance;

  @Override
  public void onCreate() {
    sInstance = this;
    super.onCreate();

    DynamicColors.applyToActivitiesIfAvailable(this);
  }
  
  public void openUrl(String url) {
    Intent intent = new Intent();
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setAction(Intent.ACTION_VIEW);
    intent.setData(Uri.parse(url));
    startActivity(intent);
  }

  public static App getInstance() {
    return sInstance;
  }
}
