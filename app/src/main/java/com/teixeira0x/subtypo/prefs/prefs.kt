/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.prefs

import androidx.appcompat.app.AppCompatDelegate
import com.teixeira0x.subtypo.App

const val PREF_CONFIGURE_GENERAL_KEY = "pref_configure_general_key"
const val PREF_ABOUT_GITHUB_KEY = "pref_about_github_key"
const val PREF_ABOUT_LIBRARIES_KEY = "pref_about_libraries_key"
const val PREF_ABOUT_VERSION_KEY = "pref_about_version_key"

const val PREF_APARENCE_UI_MODE_KEY = "pref_aparence_ui_mode_key"
const val PREF_APARENCE_MATERIALYOU_KEY = "pref_aparence_materialyou_key"

val defaultPrefs = App.instance.defaultPrefs

val aparenceUIMode: Int
  get() =
    when (defaultPrefs.getInt(PREF_APARENCE_UI_MODE_KEY, 0)) {
      1 -> AppCompatDelegate.MODE_NIGHT_NO
      2 -> AppCompatDelegate.MODE_NIGHT_YES
      else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

val aparenceMaterialYou: Boolean
  get() = defaultPrefs.getBoolean(PREF_APARENCE_MATERIALYOU_KEY, true)
