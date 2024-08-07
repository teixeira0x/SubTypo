package com.teixeira.subtitles.preferences.fragments;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.teixeira.subtitles.App;
import com.teixeira.subtitles.BuildConfig;
import com.teixeira.subtitles.R;

public class PreferencesFragment extends PreferenceFragmentCompat {

  public static final String FRAGMENT_TAG = "PreferencesFragment";

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    Preference generalPref = findPreference("pref_general");
    generalPref.setOnPreferenceClickListener(
        pref -> {
          changePreferenceFragment(new GeneralPreferencesFragment());
          return true;
        });

    Preference developmentPref = findPreference("pref_development");
    developmentPref.setOnPreferenceClickListener(
        pref -> {
          changePreferenceFragment(new DevelopmentPreferencesFragment());
          return true;
        });

    Preference githubPref = findPreference("pref_github");
    githubPref.setOnPreferenceClickListener(
        pref -> {
          App.getInstance().openUrl(App.APP_REPO_URL);
          return true;
        });

    Preference versionPref = findPreference("pref_version");
    versionPref.setSummary(createVersionText());
  }

  private String createVersionText() {
    String buildVersion = BuildConfig.VERSION_NAME;
    String buildType = BuildConfig.BUILD_TYPE;

    return buildVersion.concat(" (".concat(buildType.concat(")")));
  }

  private void changePreferenceFragment(PreferenceFragmentCompat fragment) {
    getParentFragmentManager()
        .beginTransaction()
        .replace(R.id.container, fragment)
        .addToBackStack(null)
        .commit();
  }
}
