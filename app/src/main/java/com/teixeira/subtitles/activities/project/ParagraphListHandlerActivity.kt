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

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.blankj.utilcode.util.ThreadUtils
import com.teixeira.subtitles.adapters.ParagraphListAdapter
import com.teixeira.subtitles.fragments.dialogs.ParagraphEditorDialogFragment
import com.teixeira.subtitles.subtitle.models.Subtitle
import com.teixeira.subtitles.utils.UiUtils

/**
 * Base class for ProjectActivity that handles most things related to the caption list.
 *
 * @author Felipe Teixeira
 */
abstract class ParagraphListHandlerActivity : VideoHandlerActivity() {

  private val paragraphListAdapter by lazy {
    val touchHelper = ItemTouchHelper(SubtitleTouchHelper())
    touchHelper.attachToRecyclerView(binding.paragraphs)

    ParagraphListAdapter(
      touchHelper,
      { _, index, paragraph ->
        videoViewModel.pauseVideo()
        ParagraphEditorDialogFragment.newInstance(videoViewModel.currentPosition, index, paragraph)
          .show(supportFragmentManager, null)
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

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
  }

  override fun postDestroy() {
    super.postDestroy()

    paragraphListAdapter.unregisterAdapterDataObserver(adapterDataObserver)
  }

  override fun requireParagraphListAdapter(): ParagraphListAdapter = paragraphListAdapter

  private fun setSubtitlesViewModelObservers() {
    subtitlesViewModel.observeSubtitles(this) { if (it != null) saveProjectAsync() }
    subtitlesViewModel.observeCurrentSubtitle(this) { (_, paragraph) ->
      val paragraphs = paragraph?.paragraphs
      paragraphListAdapter.paragraphs = paragraphs
      paragraphListAdapter.notifyDataSetChanged()

      binding.noParagraphs.isVisible = paragraphs?.isEmpty() ?: true
      binding.timeLine.setParagraphs(paragraphs)

      updateVideoUI(videoViewModel.currentPosition)
    }

    subtitlesViewModel.observeUndoButtonState(this) { canUndo ->
      UiUtils.setImageEnabled(binding.controllerContent.undo, canUndo)
    }

    subtitlesViewModel.observeRedoButtonState(this) { canRedo ->
      UiUtils.setImageEnabled(binding.controllerContent.redo, canRedo)
    }

    subtitlesViewModel.observeVideoParagraphIndex(this) { index ->
      paragraphListAdapter.videoParagraphIndex = index
    }

    subtitlesViewModel.observeScrollPosition(this) { position ->
      binding.paragraphs.scrollToPosition(position)
    }
  }

  inner class AdapterDataObserver(val saveProjectCallback: Runnable) :
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

    private var subtitle: Subtitle? = null

    override fun isLongPressDragEnabled(): Boolean = false

    override fun onMove(
      recyclerView: RecyclerView,
      holder: ViewHolder,
      target: ViewHolder,
    ): Boolean {
      paragraphListAdapter.notifyItemMoved(holder.layoutPosition, target.layoutPosition)
      subtitle?.swapParagraph(holder.layoutPosition, target.layoutPosition)
      return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
      val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
      return makeMovementFlags(dragFlags, 0)
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
      super.onSelectedChanged(viewHolder, actionState)
      if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
        subtitle = subtitlesViewModel.subtitle
      } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
        subtitlesViewModel.subtitle = subtitle
        subtitle = null
      }
    }

    override fun onSwiped(p0: ViewHolder, p1: Int) {}
  }
}
