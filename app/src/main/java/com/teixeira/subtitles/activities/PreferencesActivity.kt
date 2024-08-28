package com.teixeira.subtitles.activities

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.teixeira.subtitles.R
import com.teixeira.subtitles.databinding.ActivityPreferencesBinding
import com.teixeira.subtitles.preferences.fragments.PreferencesFragment

class PreferencesActivity : BaseActivity() {

  private var binding: ActivityPreferencesBinding? = null

  override fun bindView(): View {
    binding = ActivityPreferencesBinding.inflate(layoutInflater)
    return binding!!.root
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setSupportActionBar(binding!!.toolbar)
    supportActionBar?.setTitle(R.string.settings)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    binding!!.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

    val fragmentManager = supportFragmentManager
    if (fragmentManager.findFragmentByTag(PreferencesFragment.FRAGMENT_TAG) == null) {
      supportFragmentManager.commit {
        add(R.id.container, PreferencesFragment(), PreferencesFragment.FRAGMENT_TAG)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    binding = null
  }
}
