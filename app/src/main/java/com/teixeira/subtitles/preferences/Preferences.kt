package com.teixeira.subtitles.preferences

import androidx.appcompat.app.AppCompatDelegate
import com.teixeira.subtitles.App

const val PREF_CONFIGURE_GENERAL_KEY = "pref_configure_general_key"
const val PREF_ABOUT_GITHUB_KEY = "pref_about_github_key"
const val PREF_ABOUT_VERSION_KEY = "pref_about_version_key"

const val PREF_APARENCE_UI_MODE_KEY = "pref_aparence_ui_mode_key"
const val PREF_APARENCE_MATERIALYOU_KEY = "pref_aparence_materialyou_key"

val defaultPrefs = App.getInstance().getDefaultPrefs()

val aparenceUIMode: Int
  get() =
    when (defaultPrefs.getInt(PREF_APARENCE_UI_MODE_KEY, 0)) {
      1 -> AppCompatDelegate.MODE_NIGHT_NO
      2 -> AppCompatDelegate.MODE_NIGHT_YES
      else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

val aparenceMaterialYou: Boolean
  get() = defaultPrefs.getBoolean(PREF_APARENCE_MATERIALYOU_KEY, true)
