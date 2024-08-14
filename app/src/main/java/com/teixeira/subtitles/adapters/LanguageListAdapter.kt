package com.teixeira.subtitles.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.teixeira.subtitles.databinding.LayoutLanguageItemBinding
import com.teixeira.subtitles.subtitle.models.TimedTextObject

class LanguageListAdapter(
  val languages: List<TimedTextObject>,
  val onClickListener: (index: Int, timedTextObject: TimedTextObject) -> Unit,
  val onLongClickListener: (index: Int, timedTextObject: TimedTextObject) -> Boolean
) : RecyclerView.Adapter<LanguageListAdapter.VH>() {

  inner class VH(val binding: LayoutLanguageItemBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return VH(LayoutLanguageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    holder.binding.apply {
      val timedTextObject = languages[position]
      val timedTextInfo = timedTextObject.timedTextInfo

      name.text = timedTextInfo.name
      language.text = timedTextInfo.language
      format.text = timedTextInfo.format.extension
      root.setOnClickListener {
        onClickListener(languages.indexOf(timedTextObject), timedTextObject)
      }
      root.setOnLongClickListener {
        onLongClickListener(languages.indexOf(timedTextObject), timedTextObject)
      }
    }
  }

  override fun getItemCount(): Int = languages.size
}
