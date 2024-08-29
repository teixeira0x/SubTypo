package com.teixeira.subtitles.activities

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.google.android.material.color.MaterialColors
import com.teixeira.subtitles.R
import com.teixeira.subtitles.databinding.ActivityMainBinding
import com.teixeira.subtitles.fragments.dialogs.ProjectEditorDialogFragment
import com.teixeira.subtitles.handlers.PermissionsHandler
import com.teixeira.subtitles.viewmodels.MainViewModel

class MainActivity : BaseActivity() {

  private var _binding: ActivityMainBinding? = null
  private val binding: ActivityMainBinding
    get() = checkNotNull(_binding) { "MainActivity has been destroyed!" }

  private val mainViewModel by viewModels<MainViewModel>()

  private val onBackPressedCallback =
    object : OnBackPressedCallback(false) {
      override fun handleOnBackPressed() {
        if (mainViewModel.currentFragmentIndex != MainViewModel.FRAGMENT_PROJECTS_INDEX) {
          mainViewModel.currentFragmentIndex = MainViewModel.FRAGMENT_PROJECTS_INDEX
        }
      }
    }

  override val statusBarColor: Int
    get() =
      MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceInverse, 0)

  override val navigationBarColor: Int
    get() =
      MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceInverse, 0)

  override val navigationBarDividerColor: Int
    get() =
      MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceInverse, 0)

  override fun bindView(): View {
    _binding = ActivityMainBinding.inflate(layoutInflater)
    return binding.root
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)

    lifecycle.addObserver(PermissionsHandler(this, activityResultRegistry))
    onBackPressedDispatcher.addCallback(onBackPressedCallback)
    mainViewModel.observeCurrentFragmentIndex(this) { onFragmentChanged(it) }

    configureListeners()
  }

  override fun onDestroy() {
    super.onDestroy()
    onBackPressedCallback.isEnabled = false
    _binding = null
  }

  private fun configureListeners() {
    binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

    binding.fabNewProject.setOnClickListener {
      mainViewModel.currentFragmentIndex = MainViewModel.FRAGMENT_PROJECTS_INDEX
      ProjectEditorDialogFragment.newInstance().show(supportFragmentManager, null)
    }

    binding.bottomNavigation.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.item_projects ->
          mainViewModel.currentFragmentIndex = MainViewModel.FRAGMENT_PROJECTS_INDEX
        R.id.item_settings ->
          mainViewModel.currentFragmentIndex = MainViewModel.FRAGMENT_SETTINGS_INDEX
        else -> false
      }
      true
    }
  }

  private fun onFragmentChanged(fragmentIndex: Int) {
    val isProjectsFragment = fragmentIndex == MainViewModel.FRAGMENT_PROJECTS_INDEX
    if (isProjectsFragment) {
      supportActionBar?.setTitle(R.string.projects)
      setNavigationSelectedItem(R.id.item_projects)
    } else {
      supportActionBar?.setTitle(R.string.settings)
      setNavigationSelectedItem(R.id.item_settings)
    }
    binding.fragmentsContainer.displayedChild = fragmentIndex

    supportActionBar?.setDisplayHomeAsUpEnabled(!isProjectsFragment)
    supportActionBar?.setHomeButtonEnabled(!isProjectsFragment)
    onBackPressedCallback.isEnabled = !isProjectsFragment
  }

  private fun setNavigationSelectedItem(id: Int) {
    if (binding.bottomNavigation.selectedItemId != id) {
      binding.bottomNavigation.setSelectedItemId(id)
    }
  }
}
