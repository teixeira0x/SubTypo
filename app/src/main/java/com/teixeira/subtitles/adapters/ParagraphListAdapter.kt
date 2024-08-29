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

package com.teixeira.subtitles.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.teixeira.subtitles.databinding.LayoutParagraphItemBinding
import com.teixeira.subtitles.subtitle.models.Paragraph

class ParagraphListAdapter(
  val touchHelper: ItemTouchHelper,
  val onClickListener: (view: View, position: Int, paragraph: Paragraph) -> Unit,
  val onLongClickListener: (view: View, position: Int, paragraph: Paragraph) -> Boolean,
) : RecyclerView.Adapter<ParagraphListAdapter.ParagraphViewHolder>() {

  var paragraphs: List<Paragraph>? = null
  var isVideoPlaying: Boolean = false

  var videoParagraphIndex: Int = -1
    set(value) {
      if (field != value) {
        val lastVideoParagraphIndex = field
        field = value

        if (lastVideoParagraphIndex >= 0) {
          notifyItemChanged(lastVideoParagraphIndex)
        }

        if (value >= 0) {
          notifyItemChanged(value)
        }
      }
    }

  inner class ParagraphViewHolder(val binding: LayoutParagraphItemBinding) :
    RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParagraphViewHolder {
    return ParagraphViewHolder(
      LayoutParagraphItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
  }

  override fun onBindViewHolder(holder: ParagraphViewHolder, position: Int) {
    holder.binding.apply {
      val paragraph = paragraphs!![position]
      text.text = paragraph.text
      inScreen.isVisible = position == videoParagraphIndex
      startAndEnd.text = "${paragraph.startTime.time} | ${paragraph.endTime.time}"

      dragHandler.setOnLongClickListener {
        if (!isVideoPlaying) {
          touchHelper.startDrag(holder)
        }
        true
      }
      root.setOnClickListener { view ->
        onClickListener(view, paragraphs!!.indexOf(paragraph), paragraph)
      }
      root.setOnLongClickListener { view ->
        onLongClickListener(view, paragraphs!!.indexOf(paragraph), paragraph)
      }
    }
  }

  override fun getItemCount(): Int = paragraphs?.size ?: 0
}
