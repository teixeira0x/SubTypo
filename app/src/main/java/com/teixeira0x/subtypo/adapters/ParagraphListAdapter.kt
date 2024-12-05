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

package com.teixeira0x.subtypo.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.adapters.holders.BindingViewHolder
import com.teixeira0x.subtypo.databinding.LayoutParagraphItemBinding
import com.teixeira0x.subtypo.subtitle.models.Paragraph
import com.teixeira0x.subtypo.utils.ContextUtils.layoutInflater

typealias ParagraphViewHolder = BindingViewHolder<LayoutParagraphItemBinding>

class ParagraphListAdapter(
  val touchHelper: ItemTouchHelper,
  val onClickListener: (view: View, position: Int, paragraph: Paragraph) -> Unit,
  val onLongClickListener: (view: View, position: Int, paragraph: Paragraph) -> Boolean,
) : RecyclerView.Adapter<ParagraphViewHolder>() {

  private var videoParagraphIndexer = mutableListOf<Int>()
  var paragraphs: List<Paragraph>? = null
  var isVideoPlaying: Boolean = false

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParagraphViewHolder {
    return ParagraphViewHolder(
      LayoutParagraphItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )
  }

  override fun onBindViewHolder(holder: ParagraphViewHolder, position: Int) {
    holder.binding.apply {
      val paragraph = paragraphs!![position]
      text.text = paragraph.text
      startAndEnd.text = "${paragraph.startTime.formattedTime}|${paragraph.endTime.formattedTime}"

      inVideo.visibility =
        if (videoParagraphIndexer.contains(position)) View.VISIBLE else View.INVISIBLE

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

  fun setVideoPosition(videoPosition: Long) {
    val lastVideoParagraphIndexer = videoParagraphIndexer.toList()
    videoParagraphIndexer.clear()

    paragraphs?.forEach {
      if (videoPosition >= it.startTime.milliseconds && videoPosition <= it.endTime.milliseconds) {
        videoParagraphIndexer.add(paragraphs!!.indexOf(it))
      }
    }

    if (lastVideoParagraphIndexer != videoParagraphIndexer) {
      lastVideoParagraphIndexer.forEach { notifyItemChanged(it) }
      videoParagraphIndexer.forEach { notifyItemChanged(it) }
    }
  }
}
