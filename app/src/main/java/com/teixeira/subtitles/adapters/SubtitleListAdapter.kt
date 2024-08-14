package com.teixeira.subtitles.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.teixeira.subtitles.databinding.LayoutSubtitleItemBinding
import com.teixeira.subtitles.subtitle.models.Subtitle

class SubtitleListAdapter(
  val touchHelper: ItemTouchHelper,
  val onClickListener: (view: View, position: Int, subtitle: Subtitle) -> Unit,
  val onLongClickListener: (view: View, position: Int, subtitle: Subtitle) -> Boolean
) : RecyclerView.Adapter<SubtitleListAdapter.VH>() {

  var subtitles: List<Subtitle>? = null
  var isVideoPlaying: Boolean = false

  var videoSubtitleIndex: Int = -1
    set(value) {
      if (field != value) {
        val lastVideoSubtitleIndex = field
        field = value

        if (lastVideoSubtitleIndex >= 0) {
          notifyItemChanged(lastVideoSubtitleIndex)
        }

        if (value >= 0) {
          notifyItemChanged(value)
        }
      }
    }

  inner class VH(val binding: LayoutSubtitleItemBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return VH(LayoutSubtitleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    holder.binding.apply {
      val subtitle = subtitles!![position]
      text.text = subtitle.text
      inScreen.isVisible = position == videoSubtitleIndex
      startAndEnd.text = "${subtitle.startTime.time} | ${subtitle.endTime.time}"

      dragHandler.setOnLongClickListener {
        if (!isVideoPlaying) {
          touchHelper.startDrag(holder)
        }
        true
      }
      root.setOnClickListener { view ->
        onClickListener(view, subtitles!!.indexOf(subtitle), subtitle)
      }
      root.setOnLongClickListener { view ->
        onLongClickListener(view, subtitles!!.indexOf(subtitle), subtitle)
      }
    }
  }

  override fun getItemCount(): Int = subtitles?.size ?: 0
}
