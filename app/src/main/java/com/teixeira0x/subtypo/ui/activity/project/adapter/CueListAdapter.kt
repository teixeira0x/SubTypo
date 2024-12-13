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
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.databinding.LayoutCueItemBinding
import com.teixeira0x.subtypo.domain.model.Cue
import com.teixeira0x.subtypo.ui.adapter.holder.BindingViewHolder
import com.teixeira0x.subtypo.utils.ContextUtils.layoutInflater
import com.teixeira0x.subtypo.utils.TimeUtils.getFormattedTime

typealias CueViewHolder = BindingViewHolder<LayoutCueItemBinding>

class CueListAdapter(private val onCueClick: (Int, Cue) -> Unit) :
  RecyclerView.Adapter<CueViewHolder>() {

  private var cues: List<Cue>? = null

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ): CueViewHolder {
    return CueViewHolder(
      LayoutCueItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )
  }

  override fun onBindViewHolder(holder: CueViewHolder, position: Int) {
    holder.binding.apply {
      with(cues!![position]) {
        tvText.setText(text)
        tvTime.setText(
          "${startTime.getFormattedTime()}|${endTime.getFormattedTime()}"
        )

        root.setOnClickListener { onCueClick(position, this@with) }
      }
    }
  }

  override fun getItemCount() = cues?.size ?: 0

  fun submitList(cues: List<Cue>?) {
    this.cues = cues
    notifyDataSetChanged()
  }
}
