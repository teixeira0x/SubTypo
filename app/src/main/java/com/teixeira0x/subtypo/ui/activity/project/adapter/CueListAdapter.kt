package com.teixeira0x.subtypo.ui.activity.project.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.databinding.LayoutCueItemBinding
import com.teixeira0x.subtypo.domain.model.Cue
import com.teixeira0x.subtypo.ui.adapter.holder.BindingViewHolder
import com.teixeira0x.subtypo.utils.ContextUtils.layoutInflater

typealias CueViewHolder = BindingViewHolder<LayoutCueItemBinding>

class CueListAdapter() : RecyclerView.Adapter<CueViewHolder>() {

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
        tvTime.setText("$startTime|$endTime")
      }
    }
  }

  override fun getItemCount() = cues?.size ?: 0

  fun submitList(cues: List<Cue>?) {
    this.cues = cues
    notifyDataSetChanged()
  }
}
