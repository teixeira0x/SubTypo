package com.teixeira.subtitles.models;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoInfo implements Parcelable {

  private int currentVideoPosition;

  public VideoInfo(int currentVideoPosition) {
    this.currentVideoPosition = currentVideoPosition;
  }

  public VideoInfo(Parcel parcel) {
    this(parcel.readInt());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeInt(currentVideoPosition);
  }

  public int getCurrentVideoPosition() {
    return this.currentVideoPosition;
  }

  public void setCurrentVideoPosition(int currentVideoPosition) {
    this.currentVideoPosition = currentVideoPosition;
  }

  public static final Parcelable.Creator<VideoInfo> CREATOR =
      new Parcelable.Creator<VideoInfo>() {

        @Override
        public VideoInfo createFromParcel(Parcel parcel) {
          return new VideoInfo(parcel);
        }

        @Override
        public VideoInfo[] newArray(int size) {
          return new VideoInfo[size];
        }
      };
}
