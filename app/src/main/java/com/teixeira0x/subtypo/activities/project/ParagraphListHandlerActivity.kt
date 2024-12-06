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

package com.teixeira0x.subtypo.activities.project

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.blankj.utilcode.util.ThreadUtils
import com.teixeira0x.subtypo.adapters.ParagraphListAdapter
import com.teixeira0x.subtypo.subtitle.models.Subtitle

/**
 * Base class for ProjectActivity that handles most things related to the
 * caption list.
 *
 * @author Felipe Teixeira
 */
abstract class ParagraphListHandlerActivity : VideoHandlerActivity() {

  companion object {
    const val AUTOSAVE_INTERVAL_MS = 15L
  }

  private val paragraphListAdapter by lazy {
    val touchHelper = ItemTouchHelper(ParagraphTouchHelper())
    touchHelper.attachToRecyclerView(binding.paragraphs)

    ParagraphListAdapter(
      touchHelper,
      { _, index, _ ->
        videoViewModel.pauseVideo()
        showParagraphEditorSheet(index)
      },
    ) { _, _, _ ->
      true
    }
  }

  private val adapterDataObserver = AdapterDataObserver { saveProjectAsync() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding.paragraphs.layoutManager = LinearLayoutManager(this)
    binding.paragraphs.adapter = paragraphListAdapter

    paragraphListAdapter.registerAdapterDataObserver(adapterDataObserver)
    setSubtitlesViewModelObservers()
  }

  override fun postDestroy() {
    super.postDestroy()

    paragraphListAdapter.unregisterAdapterDataObserver(adapterDataObserver)
    subtitlesViewModel.autoSave = false
  }

  override fun onInitializeProject() {
    super.onInitializeProject()
    ThreadUtils.getMainHandler().post {
      val subtitles = subtitlesViewModel.subtitles
      if (subtitles.isNotEmpty()) {
        subtitlesViewModel.setCurrentSubtitle(0, subtitles[0])
      }
      subtitlesViewModel.autoSave = true
    }
  }

  override fun requireParagraphListAdapter(): ParagraphListAdapter =
    paragraphListAdapter

  private fun setSubtitlesViewModelObservers() {
    subtitlesViewModel.observeSubtitles(this) {
      if (it != null && subtitlesViewModel.autoSave) saveProjectAsync()
    }
    subtitlesViewModel.observeCurrentSubtitle(this) { (index, subtitle) ->
      supportActionBar?.subtitle =
        subtitle?.let { "${index + 1}. ${it.fullName}" }
      val paragraphs = subtitle?.paragraphs
      paragraphListAdapter.paragraphs = paragraphs
      paragraphListAdapter.notifyDataSetChanged()

      binding.noParagraphs.isVisible = paragraphs?.isEmpty() ?: true
      binding.videoContent.subtitleView.setSubtitle(subtitle)
      binding.timeLine.setParagraphs(paragraphs)

      updateVideoUI(videoViewModel.currentPosition)
    }

    subtitlesViewModel.observeUndoButtonState(this) { canUndo ->
      binding.controllerContent.undo.isEnabled = canUndo
    }

    subtitlesViewModel.observeRedoButtonState(this) { canRedo ->
      binding.controllerContent.redo.isEnabled = canRedo
    }

    subtitlesViewModel.observeScrollPosition(this) { position ->
      binding.paragraphs.scrollToPosition(position)
    }
  }

  inner class AdapterDataObserver(val saveAction: Runnable) :
    RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
      super.onChanged()
      ThreadUtils.getMainHandler().apply {
        removeCallbacks(saveAction)
        if (subtitlesViewModel.autoSave) {
          postDelayed(saveAction, AUTOSAVE_INTERVAL_MS)
        }
      }
    }
  }

  inner class ParagraphTouchHelper : ItemTouchHelper.Callback() {

    private var subtitle: Subtitle? = null

    override fun isLongPressDragEnabled(): Boolean = false

    override fun onMove(
      recyclerView: RecyclerView,
      holder: ViewHolder,
      target: ViewHolder,
    ): Boolean {
      paragraphListAdapter.notifyItemMoved(
        holder.layoutPosition,
        target.layoutPosition,
      )
      subtitle?.swapParagraph(holder.layoutPosition, target.layoutPosition)
      return true
    }

    override fun getMovementFlags(
      recyclerView: RecyclerView,
      viewHolder: ViewHolder,
    ): Int {
      val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
      return makeMovementFlags(dragFlags, 0)
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
      super.onSelectedChanged(viewHolder, actionState)
      when (actionState) {
        ItemTouchHelper.ACTION_STATE_DRAG -> {
          subtitle = subtitlesViewModel.subtitle
          subtitle?.stateManager?.startLongEdit()
        }
        ItemTouchHelper.ACTION_STATE_IDLE -> {
          subtitle?.stateManager?.endLongEdit()
          subtitlesViewModel.subtitle = subtitle
          subtitle = null
        }
        else -> {}
      }
    }

    override fun onSwiped(p0: ViewHolder, p1: Int) {
      // do nothing
    }
  }
}
