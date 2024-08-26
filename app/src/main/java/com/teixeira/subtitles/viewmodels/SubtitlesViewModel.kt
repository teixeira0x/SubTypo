package com.teixeira.subtitles.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.teixeira.subtitles.managers.UndoManager
import com.teixeira.subtitles.subtitle.models.Subtitle
import com.teixeira.subtitles.subtitle.models.TimedTextObject

class SubtitlesViewModel : ViewModel() {

  private val undoManagerLiveData = MutableLiveData<UndoManager>(UndoManager(15))
  private val updateUndoButtonsLiveData = MutableLiveData<Boolean>(false)
  private val timedTextObjectsLiveData = MutableLiveData<MutableList<TimedTextObject>>(ArrayList())
  private val selectedTimedTextObjectLiveData =
    MutableLiveData<Pair<Int, TimedTextObject?>>(-1 to null)
  private val videoSubtitleIndexLiveData = MutableLiveData<Int>(-1)
  private val scrollPositionLiveData = MutableLiveData<Int>(0)
  private val saveSubtitlesLiveData = MutableLiveData<Boolean>(false)

  var undoManager: UndoManager
    get() = undoManagerLiveData.value!!
    set(value) {
      undoManagerLiveData.value = value
    }

  var timedTextObjects: MutableList<TimedTextObject>
    get() = timedTextObjectsLiveData.value!!
    set(value) {
      timedTextObjectsLiveData.value = value
    }

  val selectedTimedTextObjectIndex: Int
    get() = selectedTimedTextObjectLiveData.value!!.first

  val selectedTimedTextObject: TimedTextObject?
    get() = selectedTimedTextObjectLiveData.value!!.second

  var isUndoManagerEnabled: Boolean
    get() = undoManager.isEnabled
    set(value) {
      val undoManager = this.undoManager
      undoManager.isEnabled = value
      this.undoManager = undoManager
    }

  var subtitles: MutableList<Subtitle>?
    get() = selectedTimedTextObject?.subtitles
    set(value) {
      val index = this.selectedTimedTextObjectIndex
      val timedTextObject = this.selectedTimedTextObject
      timedTextObject?.subtitles = value

      setSelectedTimedTextObject(index, timedTextObject)
    }

  var videoSubtitleIndex: Int
    get() = videoSubtitleIndexLiveData.value!!
    set(value) {
      if (videoSubtitleIndexLiveData.value != value) {
        videoSubtitleIndexLiveData.value = value
        scrollPosition = value
      }
    }

  var scrollPosition: Int
    get() = scrollPositionLiveData.value!!
    set(value) {
      scrollPositionLiveData.value = value
    }

  var saveSubtitles: Boolean
    get() = saveSubtitlesLiveData.value!!
    set(value) {
      saveSubtitlesLiveData.value = value
    }

  fun setSelectedTimedTextObject(index: Int, timedTextObject: TimedTextObject?) {
    selectedTimedTextObjectLiveData.value = index to timedTextObject
  }

  fun getTimedTextObject(index: Int): TimedTextObject {
    return this.timedTextObjects[index]
  }

  fun addTimedTextObject(timedTextObject: TimedTextObject, select: Boolean) {
    val timedTextObjects = this.timedTextObjects
    timedTextObjects.add(timedTextObject)
    this.timedTextObjects = timedTextObjects

    if (select) {
      setSelectedTimedTextObject(timedTextObjects.indexOf(timedTextObject), timedTextObject)
    }
  }

  fun setTimedTextObject(index: Int, timedTextObject: TimedTextObject) {
    val timedTextObjects = this.timedTextObjects
    timedTextObjects.set(index, timedTextObject)
    this.timedTextObjects = timedTextObjects
  }

  fun removeTimedTextObject(timedTextObject: TimedTextObject) {
    val timedTextObjects = this.timedTextObjects
    timedTextObjects.remove(timedTextObject)
    this.timedTextObjects = timedTextObjects

    if (timedTextObjects.isEmpty()) {
      setSelectedTimedTextObject(-1, null)
    } else if (selectedTimedTextObject == timedTextObject) {
      setSelectedTimedTextObject(0, timedTextObjects[0])
    }
  }

  fun addSubtitle(subtitle: Subtitle) {
    val subtitles = this.subtitles
    subtitles?.add(subtitle)
    this.subtitles = subtitles
  }

  fun addSubtitle(index: Int, subtitle: Subtitle) {
    val subtitles = this.subtitles
    subtitles?.add(index, subtitle)
    this.subtitles = subtitles
  }

  fun setSubtitle(index: Int, subtitle: Subtitle) {
    val subtitles = this.subtitles
    subtitles?.set(index, subtitle)
    this.subtitles = subtitles
  }

  fun removeSubtitle(subtitle: Subtitle) {
    val subtitles = this.subtitles
    subtitles?.remove(subtitle)
    this.subtitles = subtitles
  }

  fun removeSubtitle(index: Int) {
    val subtitles = this.subtitles
    subtitles?.removeAt(index)
    this.subtitles = subtitles
  }

  fun undo() {
    val undoManager = this.undoManager

    this.undoManager = undoManager
    updateUndoButtons()
  }

  fun redo() {
    val undoManager = this.undoManager

    this.undoManager = undoManager
    updateUndoButtons()
  }

  fun pushStackToUndoManager(subtitles: List<Subtitle>) {
    val undoManager = this.undoManager
    this.undoManager = undoManager
    updateUndoButtons()
  }

  fun updateUndoButtons() {
    updateUndoButtonsLiveData.value = true
  }

  fun observeUpdateUndoButtons(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
    updateUndoButtonsLiveData.observe(lifecycleOwner, observer)
  }

  fun observeSelectedTimedTextObject(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<Pair<Int, TimedTextObject?>>,
  ) {
    selectedTimedTextObjectLiveData.observe(lifecycleOwner, observer)
  }

  fun observeVideoSubtitleIndex(lifecycleOwner: LifecycleOwner, observer: Observer<Int>) {
    videoSubtitleIndexLiveData.observe(lifecycleOwner, observer)
  }

  fun observeScrollPosition(lifecycleOwner: LifecycleOwner, observer: Observer<Int>) {
    scrollPositionLiveData.observe(lifecycleOwner, observer)
  }

  fun observeSaveSubtitles(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
    saveSubtitlesLiveData.observe(lifecycleOwner, observer)
  }
}
