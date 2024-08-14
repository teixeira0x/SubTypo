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

package com.teixeira.subtitles.activities.project

import android.net.Uri
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.UriUtils
import com.teixeira.subtitles.R
import com.teixeira.subtitles.adapters.SubtitleListAdapter
import com.teixeira.subtitles.fragments.dialogs.SubtitleEditorDialogFragment
import com.teixeira.subtitles.subtitle.formats.SubtitleFormat
import com.teixeira.subtitles.subtitle.models.TimedTextInfo
import com.teixeira.subtitles.subtitle.models.TimedTextObject
import com.teixeira.subtitles.utils.DialogUtils
import com.teixeira.subtitles.utils.FileUtil
import com.teixeira.subtitles.utils.ToastUtils
import com.teixeira.subtitles.utils.UiUtils
import java.util.Collections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base class for ProjectActivity that handles most things related to the caption list.
 *
 * @author Felipe Teixeira
 */
abstract class SubtitleListHandlerActivity : VideoHandlerActivity() {

  private val subtitleListAdapter by lazy {
    val touchHelper = ItemTouchHelper(SubtitleTouchHelper())
    touchHelper.attachToRecyclerView(binding.subtitles)

    SubtitleListAdapter(
      touchHelper,
      { _, index, subtitle ->
        videoViewModel.pauseVideo()
        SubtitleEditorDialogFragment.newInstance(videoViewModel.currentPosition, index, subtitle)
          .show(supportFragmentManager, null)
      }
    ) { _, _, _ ->
      true
    }
  }

  private val subtitlesAdapterDataObserver = SubtitleAdapterDataObserver { saveProjectAsync() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding.subtitles.layoutManager = LinearLayoutManager(this)
    binding.subtitles.adapter = subtitleListAdapter

    subtitleListAdapter.registerAdapterDataObserver(subtitlesAdapterDataObserver)
    setSubtitlesViewModelObservers()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
  }

  override fun postDestroy() {
    super.postDestroy()

    subtitleListAdapter.unregisterAdapterDataObserver(subtitlesAdapterDataObserver)
  }

  override fun requireSubtitleListAdapter(): SubtitleListAdapter = subtitleListAdapter

  override fun onPickSubtitleFile(uri: Uri?) {
    super.onPickSubtitleFile(uri)

    uri ?: return

    val builder =
      DialogUtils.createSimpleDialog(
        this,
        getString(R.string.proj_import_subtitles),
        getString(R.string.msg_import_subtitles_warning)
      )

    builder.setNegativeButton(R.string.cancel, null)
    builder.setPositiveButton(R.string.ok) { _, _ ->
      val progressBuilder =
        DialogUtils.createProgressDialog(this, getString(R.string.subtitle_loading), false)
      val dialog = progressBuilder.show()

      coroutineScope.launch {
        var timedTextObject: TimedTextObject? = null
        try {
          val file = UriUtils.uri2File(uri)
          val timedTextInfo = TimedTextInfo(SubtitleFormat.SUBRIP, file.name, "en")
          timedTextObject =
            timedTextInfo.getFormat().parseFile(timedTextInfo, FileUtil.readFileContent(uri))
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            DialogUtils.createSimpleDialog(
                this@SubtitleListHandlerActivity,
                getString(R.string.error_reading_subtitles),
                e.toString()
              )
              .setPositiveButton(R.string.ok, null)
              .show()
          }
        }

        withContext(Dispatchers.Main) {
          dialog.dismiss()

          timedTextObject?.let {
            if (!it.errors.isEmpty()) {
              DialogUtils.createSimpleDialog(
                  this@SubtitleListHandlerActivity,
                  getString(R.string.error_reading_subtitles),
                  it.errors.toString()
                )
                .setPositiveButton(R.string.ok, null)
                .show()
            }
            subtitlesViewModel.pushStackToUndoManager(it.subtitles)
            subtitlesViewModel.addTimedTextObject(it, true)
          }
        }
      }
    }
    builder.show()
  }

  override fun onExportSubtitleFile(uri: Uri?) {
    super.onExportSubtitleFile(uri)

    uri ?: return

    try {
      val timedTextObject = subtitlesViewModel.selectedTimedTextObject!!
      FileUtil.writeFileContent(uri, timedTextObject.toFile())
      ToastUtils.showLong(
        R.string.proj_export_saved,
        UriUtils.uri2FileNoCacheCopy(uri).getAbsolutePath()
      )
    } catch (e: Exception) {
      DialogUtils.createSimpleDialog(
          this,
          getString(R.string.error_exporting_subtitles),
          e.toString()
        )
        .setPositiveButton(R.string.ok, null)
        .show()
    }
  }

  private fun setSubtitlesViewModelObservers() {
    subtitlesViewModel.observeSelectedTimedTextObject(this) { (_, timedTextObject) ->
      val subtitles = timedTextObject?.subtitles
      subtitleListAdapter.subtitles = subtitles
      subtitleListAdapter.notifyDataSetChanged()

      binding.noSubtitles.isVisible = subtitles?.isEmpty() ?: true
      binding.timeLine.setSubtitles(subtitles)
      updateVideoUI(videoViewModel.currentPosition)
    }

    subtitlesViewModel.observeUpdateUndoButtons(this) { _ ->
      val undoManager = subtitlesViewModel.undoManager
      UiUtils.setImageEnabled(binding.controllerContent.redo, undoManager.canRedo())
      UiUtils.setImageEnabled(binding.controllerContent.undo, undoManager.canUndo())
    }

    subtitlesViewModel.observeVideoSubtitleIndex(this) { index ->
      subtitleListAdapter.videoSubtitleIndex = index
    }

    subtitlesViewModel.observeScrollPosition(this) { position ->
      binding.subtitles.scrollToPosition(position)
    }
  }

  inner class SubtitleAdapterDataObserver(val saveProjectCallback: Runnable) :
    RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
      super.onChanged()

      if (subtitlesViewModel.saveSubtitles) {
        ThreadUtils.getMainHandler().apply {
          removeCallbacks(saveProjectCallback)
          postDelayed(saveProjectCallback, 10L)
        }
      }
    }
  }

  inner class SubtitleTouchHelper : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean = false

    override fun onMove(
      recyclerView: RecyclerView,
      holder: ViewHolder,
      target: ViewHolder
    ): Boolean {
      Collections.swap(subtitlesViewModel.subtitles!!, holder.layoutPosition, target.layoutPosition)
      subtitleListAdapter.notifyItemMoved(holder.layoutPosition, target.layoutPosition)
      return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
      val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
      return makeMovementFlags(dragFlags, 0)
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
      super.onSelectedChanged(viewHolder, actionState)
      if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
        subtitlesViewModel.pushStackToUndoManager(subtitlesViewModel.subtitles!!)
        saveProjectAsync()
      }
    }

    override fun onSwiped(p0: ViewHolder, p1: Int) {}
  }
}
