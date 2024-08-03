package com.teixeira.subtitles.preferences;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.teixeira.subtitles.App;

public class Preferences {
  public static final String PREFERENCE_UI_MODE = "pref_ui_mode";

  public static final String PREFERENCE_DEVELOPMENT_UNDO_REDO = "pref_development_undo_redo";

  public static int getUIModeValue() {
    SharedPreferences prefs = App.getInstance().getDefaultPrefs();
    switch (prefs.getInt(PREFERENCE_UI_MODE, 0)) {
      case 1:
        return AppCompatDelegate.MODE_NIGHT_NO;
      case 2:
        return AppCompatDelegate.MODE_NIGHT_YES;
    }
    return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
  }

  public static boolean isDevelopmentUndoAndRedoEnabled() {
    return App.getInstance().getDefaultPrefs().getBoolean(PREFERENCE_DEVELOPMENT_UNDO_REDO, false);
  }
}
