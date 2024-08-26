package com.teixeira.subtitles.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.teixeira.subtitles.R
import com.teixeira.subtitles.databinding.ActivityMainBinding
import com.teixeira.subtitles.fragments.dialogs.ProjectEditorDialogFragment
import com.teixeira.subtitles.handlers.PermissionsHandler

class MainActivity : BaseActivity() {

  private var _binding: ActivityMainBinding? = null
  private val binding: ActivityMainBinding
    get() = checkNotNull(_binding) { "MainActivity has been destroyed!" }

  override fun bindView(): View {
    _binding = ActivityMainBinding.inflate(layoutInflater)
    return binding.root
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)
    lifecycle.addObserver(PermissionsHandler(this, activityResultRegistry))

    binding.fabNewProject.setOnClickListener {
      ProjectEditorDialogFragment.newInstance().show(supportFragmentManager, null)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_main_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_settings -> startActivity(Intent(this, PreferencesActivity::class.java))
    }

    return true
  }
}
