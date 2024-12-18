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
import androidx.recyclerview.widget.ListAdapter
import com.teixeira0x.subtypo.databinding.LayoutSubtitleItemBinding
import com.teixeira0x.subtypo.domain.model.Subtitle
import com.teixeira0x.subtypo.ui.adapter.holder.BindingViewHolder
import com.teixeira0x.subtypo.ui.utils.ContextUtils.layoutInflater

typealias SubtitleViewHolder = BindingViewHolder<LayoutSubtitleItemBinding>

class SubtitleListAdapter(
  private val onSubtitleClick: (Subtitle) -> Unit,
  private val editSubtitle: (Subtitle) -> Unit,
) : ListAdapter<Subtitle, SubtitleViewHolder>(SubtitleDiffCallback()) {

  private var selectedId: Long = -1

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
    val subtitle = getItem(position)

    holder.binding.apply {
      tvName.text = subtitle.name
      tvFormatName.text = subtitle.format.name
      imgSelected.isVisible = subtitle.id == selectedId

      root.setOnClickListener {
        onSubtitleClick(subtitle)
        select(subtitle.id)
      }
      imgEdit.setOnClickListener { editSubtitle(subtitle) }
    }
  }

  fun submitList(subtitles: List<Subtitle>, selectId: Long) {
    submitList(subtitles)
    select(selectId)
  }

  private fun select(selectId: Long) {
    if (selectId != selectedId) {
      val previousPosition = currentList.indexOfFirst { it.id == selectedId }
      val newPosition = currentList.indexOfFirst { it.id == selectId }

      selectedId = selectId

      if (previousPosition > -1 && previousPosition < currentList.size) {
        notifyItemChanged(previousPosition) // Update previous selected item
      }
      if (newPosition > -1) {
        notifyItemChanged(newPosition) // Update new selected item
      }
    }
  }

  class SubtitleDiffCallback : DiffUtil.ItemCallback<Subtitle>() {
    override fun areItemsTheSame(
      oldItem: Subtitle,
      newItem: Subtitle,
    ): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
      oldItem: Subtitle,
      newItem: Subtitle,
    ): Boolean {
      return oldItem == newItem
    }
  }
}
