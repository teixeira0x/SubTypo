package com.teixeira.subtitles.viewmodels;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.teixeira.subtitles.managers.UndoManager;
import com.teixeira.subtitles.models.Subtitle;
import java.util.ArrayList;
import java.util.List;

public class SubtitlesViewModel extends ViewModel {

  private final MutableLiveData<UndoManager> undoManagerLiveData =
      new MutableLiveData<>(new UndoManager(15));
  private final MutableLiveData<Boolean> updateUndoButtonsLiveData = new MutableLiveData<>();

  private final MutableLiveData<List<Subtitle>> subtitlesLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<Integer> videoSubtitleIndexLiveData = new MutableLiveData<>(-1);
  private final MutableLiveData<Integer> scrollToLiveData = new MutableLiveData<>(0);

  private final MutableLiveData<Boolean> saveSubtitlesLiveData = new MutableLiveData<>();

  public UndoManager getUndoManager() {
    return undoManagerLiveData.getValue();
  }
  
  public void setSubtitles(List<Subtitle> subtitles) {
    setSubtitles(subtitles, true);
  }

  public void setSubtitles(List<Subtitle> subtitles, boolean saveSubtitles) {
    subtitlesLiveData.setValue(subtitles);

    if (saveSubtitles) {
      saveSubtitles();
    }
  }

  public List<Subtitle> getSubtitles() {
    List<Subtitle> subtitles = subtitlesLiveData.getValue();
    if (subtitles == null) {
      subtitles = new ArrayList<>();
    }
    return subtitles;
  }

  public void addSubtitle(Subtitle subtitle) {
    int index = getSubtitles().size();
    addSubtitle(index, subtitle);
  }

  public void addSubtitle(int index, Subtitle subtitle) {
    List<Subtitle> subtitles = getSubtitles();
    subtitles.add(index, subtitle);
    pushStackToUndoManager(subtitles);
    setSubtitles(subtitles);
    scrollTo(index);
  }

  public void setSubtitle(int index, Subtitle subtitle) {
    if (index >= 0) {
      List<Subtitle> subtitles = getSubtitles();

      if (index < subtitles.size()) {
        subtitles.remove(index);
        subtitles.add(index, subtitle);

        pushStackToUndoManager(subtitles);
        setSubtitles(subtitles);
        scrollTo(index);
      }
    }
  }

  public void setVideoSubtitleIndex(int index) {
    if (index != videoSubtitleIndexLiveData.getValue()) {
      videoSubtitleIndexLiveData.setValue(index);
      scrollTo(index);
    }
  }

  public void removeSubtitle(int index) {
    if (index >= 0) {
      List<Subtitle> subtitles = getSubtitles();
      if (index < subtitles.size()) {
        subtitles.remove(index);
        pushStackToUndoManager(subtitles);
        setSubtitles(subtitles);
      }
    }
  }

  public void removeSubtitle(Subtitle subtitle) {
    List<Subtitle> subtitles = getSubtitles();

    if (subtitles.remove(subtitle)) {
      pushStackToUndoManager(subtitles);
      setSubtitles(subtitles);
    }
  }

  public void undo() {
    UndoManager undoManager = undoManagerLiveData.getValue();
    List<Subtitle> subtitles = undoManager.undo();
    if (subtitles != null) {
      setSubtitles(subtitles);
    }
    undoManagerLiveData.setValue(undoManager);
    updateUndoButtons();
  }

  public void redo() {
    UndoManager undoManager = undoManagerLiveData.getValue();
    List<Subtitle> subtitles = undoManager.redo();
    if (subtitles != null) {
      setSubtitles(subtitles);
    }
    undoManagerLiveData.setValue(undoManager);
    updateUndoButtons();
  }

  public void pushStackToUndoManager(List<Subtitle> subtitles) {
    UndoManager undoManager = undoManagerLiveData.getValue();
    undoManager.pushStack(subtitles);
    undoManagerLiveData.setValue(undoManager);
    updateUndoButtons();
  }

  public void updateUndoButtons() {
    updateUndoButtonsLiveData.setValue(true);
  }

  public void scrollTo(int position) {
    scrollToLiveData.setValue(position);
  }

  public void saveSubtitles() {
    saveSubtitlesLiveData.setValue(true);
  }

  public void observeUpdateUndoButtons(LifecycleOwner lifecycleOwner, Observer<Boolean> observer) {
    updateUndoButtonsLiveData.observe(lifecycleOwner, observer);
  }

  public void observeSubtitles(LifecycleOwner lifecycleOwner, Observer<List<Subtitle>> observer) {
    subtitlesLiveData.observe(lifecycleOwner, observer);
  }

  public void observeVideoSubtitleIndex(LifecycleOwner lifecycleOwner, Observer<Integer> observer) {
    videoSubtitleIndexLiveData.observe(lifecycleOwner, observer);
  }

  public void observeScrollTo(LifecycleOwner lifecycleOwner, Observer<Integer> observer) {
    scrollToLiveData.observe(lifecycleOwner, observer);
  }

  public void observeSaveSubtitles(LifecycleOwner lifecycleOwner, Observer<Boolean> observer) {
    saveSubtitlesLiveData.observe(lifecycleOwner, observer);
  }
}
