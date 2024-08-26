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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import com.teixeira.subtitles.databinding.LayoutLanguageItemBinding
import com.teixeira.subtitles.subtitle.models.TimedTextObject

class LanguageListAdapter(
  val languages: List<TimedTextObject>,
  val selectedIndex: Int,
  val onClickListener: (view: View, index: Int, timedTextObject: TimedTextObject) -> Unit,
  val onLongClickListener: (index: Int, timedTextObject: TimedTextObject) -> Boolean,
) : RecyclerView.Adapter<LanguageListAdapter.VH>() {

  inner class VH(val binding: LayoutLanguageItemBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return VH(LayoutLanguageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    holder.binding.apply {
      val timedTextObject = languages[position]
      val timedTextInfo = timedTextObject.timedTextInfo

      name.text = timedTextInfo.name + timedTextInfo.format.extension
      language.text = timedTextInfo.language

      if (selectedIndex == position) {
        root.setCardBackgroundColor(MaterialColors.getColor(root, R.attr.colorOutlineVariant))
      }

      edit.setOnClickListener {
        onClickListener(it, languages.indexOf(timedTextObject), timedTextObject)
      }

      root.setOnClickListener {
        onClickListener(it, languages.indexOf(timedTextObject), timedTextObject)
      }
      root.setOnLongClickListener {
        onLongClickListener(languages.indexOf(timedTextObject), timedTextObject)
      }
    }
  }

  override fun getItemCount(): Int = languages.size
}
