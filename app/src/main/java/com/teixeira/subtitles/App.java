package com.teixeira.subtitles;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import com.google.android.material.color.DynamicColors;
import com.teixeira.subtitles.preferences.Preferences;

public class App extends Application {

  private static App sInstance;

  private SharedPreferences defaultPrefs;

  @Override
  public void onCreate() {
    sInstance = this;
    super.onCreate();

    defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    DynamicColors.applyToActivitiesIfAvailable(this);
    updateUIMode();
  }

  public void updateUIMode() {
    AppCompatDelegate.setDefaultNightMode(Preferences.getUIModeValue());
  }

  public void openUrl(String url) {
    Intent intent = new Intent();
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setAction(Intent.ACTION_VIEW);
    intent.setData(Uri.parse(url));
    startActivity(intent);
  }

  public SharedPreferences getDefaultPrefs() {
    return this.defaultPrefs;
  }

  public static App getInstance() {
    return sInstance;
  }
}
