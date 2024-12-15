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

package com.teixeira0x.subtypo.ui.activity.project.adapter

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.databinding.LayoutSubtitleItemBinding
import com.teixeira0x.subtypo.domain.model.Subtitle
import com.teixeira0x.subtypo.ui.adapter.holder.BindingViewHolder
import com.teixeira0x.subtypo.ui.utils.ContextUtils.layoutInflater

typealias SubtitleViewHolder = BindingViewHolder<LayoutSubtitleItemBinding>

class SubtitleListAdapter(
  private val onSubtitleClick: (Subtitle) -> Unit,
  private val editSubtitle: (Subtitle) -> Unit,
  private val deleteSubtitle: (Subtitle) -> Unit,
) : RecyclerView.Adapter<SubtitleViewHolder>() {

  private var subtitles: List<Subtitle> = emptyList()
  private var selectedId: Long = 0

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ): SubtitleViewHolder {
    return SubtitleViewHolder(
      LayoutSubtitleItemBinding.inflate(
        parent.context.layoutInflater,
        parent,
        false,
      )
    )
  }

  override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int) {
    holder.binding.apply {
      val subtitle = subtitles[position]

      tvName.text = subtitle.name
      imgSelected.isVisible = subtitle.id == selectedId

      options.editOption.setOnClickListener { editSubtitle(subtitle) }
      options.deleteOption.setOnClickListener { deleteSubtitle(subtitle) }

      root.setOnClickListener { onSubtitleClick(subtitle) }
      root.setOnLongClickListener { toggleItemOptionsVisibility() }
      imgChevron.setOnClickListener { toggleItemOptionsVisibility() }
    }
  }

  override fun getItemCount() = subtitles.size

  private fun LayoutSubtitleItemBinding.toggleItemOptionsVisibility(): Boolean {
    optionsContainer.isVisible = !optionsContainer.isVisible
    imgChevron
      .animate()
      .rotation(if (optionsContainer.isVisible) 0f else -90f)
      .start()

    return true
  }

  fun submitList(subtitles: List<Subtitle>, selectedId: Long = 0) {
    val result =
      DiffUtil.calculateDiff(
        SubtitleDiffCallback(
          this.selectedId,
          selectedId,
          this.subtitles,
          subtitles,
        )
      )
    this.subtitles = subtitles
    this.selectedId = selectedId
    result.dispatchUpdatesTo(this)
  }

  class SubtitleDiffCallback(
    val oldSelectedId: Long,
    val newSelectedId: Long,
    val oldList: List<Subtitle>,
    val newList: List<Subtitle>,
  ) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
      return oldList[oldPosition] == newList[newPosition]
    }

    override fun areContentsTheSame(
      oldPosition: Int,
      newPosition: Int,
    ): Boolean {
      val oldSubtitle = oldList[oldPosition]
      val newSubtitle = newList[newPosition]

      return oldSubtitle.id == newSubtitle.id &&
        oldSubtitle.name == newSubtitle.name &&
        oldSelectedId == newSelectedId
    }
  }
}
