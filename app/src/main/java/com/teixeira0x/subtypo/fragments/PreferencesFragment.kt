package com.teixeira0x.subtypo.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.teixeira0x.subtypo.App
import com.teixeira0x.subtypo.BuildConfig
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.preferences.PREF_ABOUT_GITHUB_KEY
import com.teixeira0x.subtypo.preferences.PREF_ABOUT_VERSION_KEY
import com.teixeira0x.subtypo.preferences.PREF_CONFIGURE_GENERAL_KEY
import com.teixeira0x.subtypo.viewmodels.MainViewModel
import com.teixeira0x.subtypo.viewmodels.PreferencesViewModel

class PreferencesFragment : PreferenceFragmentCompat() {

  companion object {
    const val FRAGMENT_TAG = "PreferencesFragment"
  }

  private val mainViewModel by viewModels<MainViewModel>(ownerProducer = { requireActivity() })
  private val preferencesViewModel by viewModels<PreferencesViewModel>()

  private val onBackPressedCallback =
    object : OnBackPressedCallback(false) {
      override fun handleOnBackPressed() {
        if (preferencesViewModel.currentPreferencesId != R.xml.preferences) {
          preferencesViewModel.currentPreferencesId = R.xml.preferences
        }
      }
    }

  private val versionSummary: String
    get() = "${com.teixeira0x.subtypo.BuildConfig.VERSION_NAME} (${com.teixeira0x.subtypo.BuildConfig.BUILD_TYPE.uppercase()})"

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    preferencesViewModel.observeCurrentPreferencesId(this) {
      onBackPressedCallback.isEnabled = it != R.xml.preferences
      setPreferencesFromResource(it, rootKey)
      onPreferencesIdChange(it)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

    mainViewModel.observeCurrentFragmentIndex(this) {
      onBackPressedCallback.isEnabled =
        it == MainViewModel.FRAGMENT_SETTINGS_INDEX &&
          preferencesViewModel.currentPreferencesId != R.xml.preferences
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    onBackPressedCallback.isEnabled = false
  }

  private fun onPreferencesIdChange(preferencesId: Int) {
    if (preferencesId == R.xml.preferences) {
      findPreference<Preference>(PREF_CONFIGURE_GENERAL_KEY)?.setOnPreferenceClickListener { _ ->
        preferencesViewModel.currentPreferencesId = R.xml.preferences_general
        true
      }

      findPreference<Preference>(PREF_ABOUT_GITHUB_KEY)?.setOnPreferenceClickListener { _ ->
        App.instance.openUrl(App.APP_REPO_URL)
        true
      }

      findPreference<Preference>(PREF_ABOUT_VERSION_KEY)?.setSummary(versionSummary)
    }
  }
}
