package com.teixeira.subtitles.preferences

import androidx.appcompat.app.AppCompatDelegate
import com.teixeira.subtitles.App

const val PREFERENCE_UI_MODE = "pref_ui_mode"

val defaultPrefs = App.getInstance().getDefaultPrefs()

val aparenceUIModeValue: Int
  get() =
    when (defaultPrefs.getInt(PREFERENCE_UI_MODE, 0)) {
      1 -> AppCompatDelegate.MODE_NIGHT_NO
      2 -> AppCompatDelegate.MODE_NIGHT_YES
      else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
