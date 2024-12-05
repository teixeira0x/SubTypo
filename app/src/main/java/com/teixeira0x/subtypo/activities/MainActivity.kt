package com.teixeira0x.subtypo.activities

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.google.android.material.color.MaterialColors
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.databinding.ActivityMainBinding
import com.teixeira0x.subtypo.fragments.sheets.ProjectEditorFragment
import com.teixeira0x.subtypo.handlers.PermissionsHandler
import com.teixeira0x.subtypo.viewmodels.MainViewModel

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
      ProjectEditorFragment.newInstance().show(supportFragmentManager, null)
    }

    binding.bottomNavigation.setOnItemSelectedListener { item ->
      mainViewModel.currentFragmentIndex =
        when (item.itemId) {
          R.id.item_projects -> MainViewModel.FRAGMENT_PROJECTS_INDEX
          R.id.item_settings -> MainViewModel.FRAGMENT_SETTINGS_INDEX
          else -> throw IllegalArgumentException("Invalid item id: '${item.itemId}'")
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
