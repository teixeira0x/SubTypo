package com.teixeira.subtitles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.teixeira.subtitles.App;
import com.teixeira.subtitles.BuildConfig;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.databinding.ActivityAboutBinding;

public class AboutActivity extends BaseActivity {

  private ActivityAboutBinding binding;

  @Override
  protected View bindView() {
    binding = ActivityAboutBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setSupportActionBar(binding.toolbar);
    getSupportActionBar().setTitle(R.string.about);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.container, new AboutPreferenceFragment())
        .commit();
  }

  public static class AboutPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.about, rootKey);

      Preference githubPref = findPreference("pref_github");
      githubPref.setOnPreferenceClickListener(
          pref -> {
            App.getInstance().openUrl("https://github.com/teixeira0x/subtitles");
            return true;
          });

      Preference licensePref = findPreference("pref_licenses");
      licensePref.setOnPreferenceClickListener(
          pref -> {
            startActivity(new Intent(requireContext(), OssLicensesMenuActivity.class));
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
  }
}
