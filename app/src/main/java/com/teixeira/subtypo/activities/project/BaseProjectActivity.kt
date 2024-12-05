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

package com.teixeira.subtypo.activities.project

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.color.MaterialColors
import com.teixeira.subtypo.R
import com.teixeira.subtypo.activities.BaseActivity
import com.teixeira.subtypo.adapters.ParagraphListAdapter
import com.teixeira.subtypo.databinding.ActivityProjectBinding
import com.teixeira.subtypo.ext.cancelIfActive
import com.teixeira.subtypo.handlers.SubtitleExporterHandler
import com.teixeira.subtypo.handlers.SubtitlePickerHandler
import com.teixeira.subtypo.viewmodels.SubtitlesViewModel
import com.teixeira.subtypo.viewmodels.VideoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Base class for ProjectActivity that handles most activity related things.
 *
 * @author Felipe Teixeira
 */
abstract class BaseProjectActivity : BaseActivity() {

  companion object {
    const val KEY_PROJECT = "key_project"
  }

  private var _binding: ActivityProjectBinding? = null
  private var _isDestroying = false

  protected val coroutineScope = CoroutineScope(Dispatchers.Main)
  protected val videoViewModel by viewModels<VideoViewModel>()
  protected val subtitlesViewModel by viewModels<SubtitlesViewModel>()

  private val subtitlePickerHandler by lazy {
    SubtitlePickerHandler(this, activityResultRegistry, subtitlesViewModel)
  }
  private val subtitleExporterHandler by lazy {
    SubtitleExporterHandler(this, activityResultRegistry, subtitlesViewModel)
  }

  protected val binding: ActivityProjectBinding
    get() = checkNotNull(_binding) { "Activity has been destroyed!" }

  protected val isDestroying: Boolean
    get() = _isDestroying

  override val statusBarColor: Int
    get() =
      MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceInverse, 0)

  override fun bindView(): View {
    _binding = ActivityProjectBinding.inflate(layoutInflater)
    return binding.root
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

    lifecycle.apply {
      addObserver(subtitlePickerHandler)
      addObserver(subtitleExporterHandler)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_project_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_import -> subtitlePickerHandler.launchPicker()
      R.id.menu_export -> subtitleExporterHandler.launchExporter()
      else -> {}
    }

    return true
  }

  override fun onDestroy() {
    preDestroy()
    super.onDestroy()
    postDestroy()
  }

  protected open fun preDestroy() {
    _isDestroying = true
  }

  protected open fun postDestroy() {
    coroutineScope.cancelIfActive("Activity has been destroyed")
    _binding = null
  }

  protected abstract fun requireParagraphListAdapter(): ParagraphListAdapter
}
