package com.teixeira.subtitles.viewmodels;

import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class VideoViewModel extends ViewModel {

  private final MutableLiveData<Pair<Integer, Boolean>> currentVideoPositionLiveData =
      new MutableLiveData<>(Pair.create(0, false));
  private final MutableLiveData<Integer> videoDurationLiveData = new MutableLiveData<>(0);
  private final MutableLiveData<Boolean> isVideoPlayingLiveData = new MutableLiveData<>(false);

  public void setCurrentVideoPosition(int currentVideoPosition, boolean seekTo) {
    currentVideoPositionLiveData.setValue(Pair.create(currentVideoPosition, seekTo));
  }

  public int getCurrentVideoPosition() {
    return this.currentVideoPositionLiveData.getValue().first;
  }

  public LiveData<Pair<Integer, Boolean>> getCurrentVideoPositionLiveData() {
    return this.currentVideoPositionLiveData;
  }

  public void observeCurrentVideoPosition(
      LifecycleOwner lifecycleOwner, Observer<Pair<Integer, Boolean>> observer) {
    currentVideoPositionLiveData.observe(lifecycleOwner, observer);
  }

  public void back5sec() {
    boolean hasVideoPlaying = isVideoPlaying();
    if (hasVideoPlaying) pauseVideo();
    int seek = getCurrentVideoPosition() - 5000;
    if (getCurrentVideoPosition() <= 5000) {
      seek = 0;
    }

    setCurrentVideoPosition(seek, true);
    if (hasVideoPlaying) playVideo();
  }

  public void skip5sec() {
    boolean hasVideoPlaying = isVideoPlaying();
    if (hasVideoPlaying) pauseVideo();
    int seek = getCurrentVideoPosition() + 5000;
    if (seek > videoDurationLiveData.getValue()) {
      seek = videoDurationLiveData.getValue();
    }

    setCurrentVideoPosition(seek, true);
    if (hasVideoPlaying) playVideo();
  }

  public void playVideo() {
    setVideoPlaying(true);
  }

  public void pauseVideo() {
    setVideoPlaying(false);
  }

  public boolean isVideoPlaying() {
    return this.isVideoPlayingLiveData.getValue();
  }

  public LiveData<Boolean> isVideoPlayingLiveData() {
    return this.isVideoPlayingLiveData;
  }

  public void setVideoPlaying(boolean isVideoPlaying) {
    isVideoPlayingLiveData.setValue(isVideoPlaying);
  }

  public void observeIsVideoPlaying(LifecycleOwner lifecycleOwner, Observer<Boolean> observer) {
    isVideoPlayingLiveData.observe(lifecycleOwner, observer);
  }

  public void setVideoDuration(int videoDuration) {
    videoDurationLiveData.setValue(videoDuration);
  }
}
