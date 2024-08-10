package com.teixeira.subtitles.viewmodels;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.teixeira.subtitles.subtitle.file.SubtitleFile;

public class ProjectViewModel extends ViewModel {

  private final MutableLiveData<SubtitleFile> selectedSubtitleFileLive = new MutableLiveData<>();

  public SubtitleFile getSelectedSubtitleFile() {
    return selectedSubtitleFileLive.getValue();
  }

  public void setSelectedSubtitleFile(SubtitleFile subtitleFile) {
    selectedSubtitleFileLive.setValue(subtitleFile);
  }
  
  public void observeSelectedSubtitleFile(LifecycleOwner lifecycleOwner, Observer<SubtitleFile> observer) {
    selectedSubtitleFileLive.observe(lifecycleOwner, observer);
  }
}
