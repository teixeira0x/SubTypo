package com.teixeira.subtitles.preferences.fragments

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.teixeira.subtitles.App
import com.teixeira.subtitles.BuildConfig
import com.teixeira.subtitles.R
import com.teixeira.subtitles.viewmodels.PreferencesViewModel

class PreferencesFragment : PreferenceFragmentCompat() {

  companion object {
    const val FRAGMENT_TAG = "PreferencesFragment"
  }

  private val preferencesViewModel by viewModels<PreferencesViewModel>()

  private val onBackPressedCallback =
    object : OnBackPressedCallback(false) {
      override fun handleOnBackPressed() {
        val currentPreferencesId = preferencesViewModel.currentPreferencesId
        if (currentPreferencesId != R.xml.preferences) {
          preferencesViewModel.currentPreferencesId = R.xml.preferences
        }
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
  }

  override fun onResume() {
    super.onResume()
    onBackPressedCallback.isEnabled = preferencesViewModel.currentPreferencesId != R.xml.preferences
  }

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    preferencesViewModel.observeCurrentPreferencesId(this) {
      onBackPressedCallback.isEnabled = it != R.xml.preferences
      setPreferencesFromResource(it, rootKey)
      onPreferencesIdChange(it)
    }
  }

  private fun onPreferencesIdChange(preferencesId: Int) {
    if (preferencesId == R.xml.preferences) {
      findPreference<Preference>("pref_general")?.setOnPreferenceClickListener { _ ->
        preferencesViewModel.currentPreferencesId = R.xml.preferences_general
        true
      }

      findPreference<Preference>("pref_development")?.setOnPreferenceClickListener { _ ->
        preferencesViewModel.currentPreferencesId = R.xml.preferences_development
        true
      }

      findPreference<Preference>("pref_github")?.setOnPreferenceClickListener { _ ->
        App.getInstance().openUrl(App.APP_REPO_URL)
        true
      }

      findPreference<Preference>("pref_version")?.setSummary(createVersionText())
    }
  }

  private fun createVersionText(): String {
    val buildVersion = BuildConfig.VERSION_NAME
    val buildType = BuildConfig.BUILD_TYPE
    return "$buildVersion ($buildType)"
  }
}
