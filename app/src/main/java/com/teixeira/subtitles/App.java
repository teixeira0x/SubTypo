package com.teixeira.subtitles;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import com.blankj.utilcode.util.ThrowableUtils;
import com.google.android.material.color.DynamicColors;
import com.teixeira.subtitles.activities.CrashActivity;
import com.teixeira.subtitles.preferences.PreferencesKt;

public class App extends Application {

  public static final String APP_REPO_URL = "https://github.com/teixeira0x/SubTypo";
  public static final String APP_REPO_OPEN_ISSUE = APP_REPO_URL + "/issues/new";

  private static App sInstance;

  private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
  private SharedPreferences defaultPrefs;

  @Override
  public void onCreate() {
    uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
    sInstance = this;
    super.onCreate();

    defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    DynamicColors.applyToActivitiesIfAvailable(this);
    updateUIMode();
  }

  public void updateUIMode() {
    AppCompatDelegate.setDefaultNightMode(PreferencesKt.getAparenceUIModeValue());
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

  public void uncaughtException(Thread thread, Throwable throwable) {
    try {
      // Start the crash activity
      Intent intent = new Intent(this, CrashActivity.class);
      intent.putExtra(CrashActivity.KEY_ERROR, ThrowableUtils.getFullStackTrace(throwable));
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);

      if (uncaughtExceptionHandler != null) {
        uncaughtExceptionHandler.uncaughtException(thread, throwable);
      }
      System.exit(1);
    } catch (Throwable th) {
      th.printStackTrace();
    }
  }
}
