package com.teixeira.subtitles.activities;

import android.os.Bundle;
import android.view.View;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.databinding.ActivityPreferencesBinding;
import com.teixeira.subtitles.preferences.fragments.PreferencesFragment;

public class PreferencesActivity extends BaseActivity {

  private ActivityPreferencesBinding binding;

  @Override
  protected View bindView() {
    binding = ActivityPreferencesBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setSupportActionBar(binding.toolbar);
    getSupportActionBar().setTitle(R.string.settings);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    if (getSupportFragmentManager().findFragmentByTag(PreferencesFragment.FRAGMENT_TAG) == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.container, new PreferencesFragment(), PreferencesFragment.FRAGMENT_TAG)
          .commit();
    }
  }
}
