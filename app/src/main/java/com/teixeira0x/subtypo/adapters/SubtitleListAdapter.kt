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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import com.teixeira0x.subtypo.adapters.holders.BindingViewHolder
import com.teixeira0x.subtypo.databinding.LayoutSubtitleItemBinding
import com.teixeira0x.subtypo.subtitle.models.Subtitle
import com.teixeira0x.subtypo.utils.ContextUtils.layoutInflater

typealias SubtitleViewHolder = BindingViewHolder<LayoutSubtitleItemBinding>

class SubtitleListAdapter(
  val subtitles: List<Subtitle>,
  val selectedIndex: Int,
  val onClickListener: (view: View, index: Int, subtitle: Subtitle) -> Unit,
  val onLongClickListener: (index: Int, subtitle: Subtitle) -> Boolean,
) : RecyclerView.Adapter<SubtitleViewHolder>() {

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

      name.text =
        "${position + 1}. ${subtitle.name}${subtitle.subtitleFormat.extension}"

      if (selectedIndex == position) {
        root.setCardBackgroundColor(
          MaterialColors.getColor(root, R.attr.colorOutlineVariant)
        )
      }

      edit.setOnClickListener {
        onClickListener(it, subtitles.indexOf(subtitle), subtitle)
      }

      root.setOnClickListener {
        onClickListener(it, subtitles.indexOf(subtitle), subtitle)
      }
      root.setOnLongClickListener {
        onLongClickListener(subtitles.indexOf(subtitle), subtitle)
      }
    }
  }

  override fun getItemCount(): Int = subtitles.size
}
