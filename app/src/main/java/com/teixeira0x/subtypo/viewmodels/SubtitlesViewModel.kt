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

package com.teixeira0x.subtypo.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.teixeira0x.subtypo.subtitle.models.Paragraph
import com.teixeira0x.subtypo.subtitle.models.Subtitle
import com.teixeira0x.subtypo.subtitle.models.Time

class SubtitlesViewModel : ViewModel() {

  private val _undoButtonState = MutableLiveData<Boolean>(false)
  private val _redoButtonState = MutableLiveData<Boolean>(false)
  private val _subtitles = MutableLiveData<MutableList<Subtitle>?>(null)
  private val _currentSubtitle =
    MutableLiveData<Pair<Int, Subtitle?>>(-1 to null)

  private val _scrollPosition = MutableLiveData<Int>(0)
  private val _autoSave = MutableLiveData<Boolean>(false)

  var subtitles: MutableList<Subtitle>
    get() = _subtitles.value ?: mutableListOf()
    set(value) {
      _subtitles.value = value
    }

  val subtitleIndex: Int
    get() = _currentSubtitle.value!!.first

  var subtitle: Subtitle?
    get() = _currentSubtitle.value!!.second
    set(value) {
      setCurrentSubtitle(subtitleIndex, value)
    }

  val paragraphs: List<Paragraph>
    get() = subtitle?.paragraphs ?: listOf()

  var scrollPosition: Int
    get() = _scrollPosition.value!!
    set(value) {
      _scrollPosition.value = value
    }

  var autoSave: Boolean
    get() = _autoSave.value!!
    set(value) {
      _autoSave.value = value
    }

  fun setCurrentSubtitle(index: Int, subtitle: Subtitle?) {
    _currentSubtitle.value = index to subtitle
    updateUndoButtons()
  }

  fun getSubtitle(index: Int): Subtitle {
    return this.subtitles[index]
  }

  fun addSubtitle(subtitle: Subtitle, select: Boolean) {
    val subtitles = this.subtitles
    subtitles.add(subtitle)
    this.subtitles = subtitles

    if (select) {
      setCurrentSubtitle(subtitles.size - 1, subtitle)
    }
  }

  fun setSubtitle(index: Int, subtitle: Subtitle) {
    val subtitles = this.subtitles
    subtitles.set(index, subtitle)
    this.subtitles = subtitles
  }

  fun removeSubtitle(subtitle: Subtitle) {
    val subtitles = this.subtitles
    subtitles.remove(subtitle)
    this.subtitles = subtitles

    if (subtitles.isNotEmpty() && this.subtitle == subtitle) {
      var newCurrentIndex = subtitleIndex - 1
      newCurrentIndex =
        if (newCurrentIndex >= subtitles.size - 1) {
          newCurrentIndex
        } else 0

      setCurrentSubtitle(newCurrentIndex, subtitles[newCurrentIndex])
    } else if (subtitles.isEmpty()) {
      setCurrentSubtitle(-1, null)
    }
  }

  fun swapParagraph(first: Int, second: Int) {
    val subtitle = this.subtitle
    subtitle?.swapParagraph(first, second)
    this.subtitle = subtitle
  }

  fun addParagraph(
    index: Int = paragraphs.size,
    startTime: Long,
    endTime: Long,
    text: String,
  ) {
    val subtitle = this.subtitle
    subtitle?.addParagraph(
      index = index,
      paragraph =
        Paragraph(
          startTime = Time(startTime),
          endTime = Time(endTime),
          text = text,
        ),
    )
    this.subtitle = subtitle
  }

  fun setParagraph(index: Int, startTime: Long, endTime: Long, text: String) {
    val subtitle = this.subtitle
    subtitle?.setParagraph(
      index = index,
      paragraph =
        Paragraph(
          startTime = Time(startTime),
          endTime = Time(endTime),
          text = text,
        ),
    )
    this.subtitle = subtitle
  }

  fun removeParagraph(index: Int) {
    val subtitle = this.subtitle
    subtitle?.removeParagraph(index)
    this.subtitle = subtitle
  }

  fun undo() {
    val subtitle = this.subtitle
    if (subtitle?.canUndo() ?: false) {
      subtitle!!.undo()
    }
    this.subtitle = subtitle
  }

  fun redo() {
    val subtitle = this.subtitle
    if (subtitle?.canRedo() ?: false) {
      subtitle!!.redo()
    }
    this.subtitle = subtitle
  }

  fun updateUndoButtons() {
    _undoButtonState.value = subtitle?.canUndo() ?: false
    _redoButtonState.value = subtitle?.canRedo() ?: false
  }

  fun observeUndoButtonState(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<Boolean>,
  ) {
    _undoButtonState.observe(lifecycleOwner, observer)
  }

  fun observeRedoButtonState(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<Boolean>,
  ) {
    _redoButtonState.observe(lifecycleOwner, observer)
  }

  fun observeSubtitles(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<List<Subtitle>?>,
  ) {
    _subtitles.observe(lifecycleOwner, observer)
  }

  fun observeCurrentSubtitle(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<Pair<Int, Subtitle?>>,
  ) {
    _currentSubtitle.observe(lifecycleOwner, observer)
  }

  fun observeScrollPosition(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<Int>,
  ) {
    _scrollPosition.observe(lifecycleOwner, observer)
  }

  fun observeAautoSave(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<Boolean>,
  ) {
    _autoSave.observe(lifecycleOwner, observer)
  }
}
