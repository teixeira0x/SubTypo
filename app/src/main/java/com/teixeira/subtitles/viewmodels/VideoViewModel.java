package com.teixeira.subtitles.viewmodels;

import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class VideoViewModel extends ViewModel {

  private final MutableLiveData<Boolean> isPreparedLiveData = new MutableLiveData<>(false);
  private final MutableLiveData<Pair<Long, Boolean>> currentPositionLiveData =
      new MutableLiveData<>(Pair.create(Long.valueOf(0), false));
  private final MutableLiveData<Long> durationLiveData = new MutableLiveData<>(Long.valueOf(0));
  private final MutableLiveData<Boolean> isPlayingLiveData = new MutableLiveData<>(false);

  public void setPrepared(boolean isPrepared) {
    isPreparedLiveData.setValue(isPrepared);
  }

  public boolean isPrepared() {
    return isPreparedLiveData.getValue();
  }

  public void setCurrentPosition(long currentPosition, boolean seekTo) {
    currentPositionLiveData.setValue(Pair.create(currentPosition, seekTo));
  }

  public long getCurrentPosition() {
    return this.currentPositionLiveData.getValue().first;
  }

  public LiveData<Pair<Long, Boolean>> getCurrentPositionLiveData() {
    return this.currentPositionLiveData;
  }

  public void observeCurrentPosition(
      LifecycleOwner lifecycleOwner, Observer<Pair<Long, Boolean>> observer) {
    currentPositionLiveData.observe(lifecycleOwner, observer);
  }

  public void playVideo() {
    setPlaying(true);
  }

  public void pauseVideo() {
    setPlaying(false);
  }

  public boolean isPlaying() {
    return this.isPlayingLiveData.getValue();
  }

  public LiveData<Boolean> isVideoPlayingLiveData() {
    return this.isPlayingLiveData;
  }

  public void setPlaying(boolean isVideoPlaying) {
    isPlayingLiveData.setValue(isVideoPlaying);
  }
  
  public void observeIsPrepared(LifecycleOwner lifecycleOwner, Observer<Boolean> observer) {
    isPreparedLiveData.observe(lifecycleOwner, observer);
  }

  public void observeIsPlaying(LifecycleOwner lifecycleOwner, Observer<Boolean> observer) {
    isPlayingLiveData.observe(lifecycleOwner, observer);
  }

  public void setDuration(long videoDuration) {
    durationLiveData.setValue(videoDuration);
  }
}
