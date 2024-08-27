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

package com.teixeira.subtitles.subtitle.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Felipe Teixeira
 */
public class Subtitle implements Parcelable {
  private Time startTime;
  private Time endTime;
  private String text;
  
  public Subtitle(long startTime, long endTime, String text) {
    this(new Time(startTime), new Time(endTime), text);
  }

  public Subtitle(Time startTime, Time endTime, String text) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.text = text;
  }

  public Subtitle(Parcel parcel) {
    this(
        parcel.readParcelable(Subtitle.class.getClassLoader()),
        parcel.readParcelable(Subtitle.class.getClassLoader()),
        parcel.readString());
  }

  public Time getStartTime() {
    return this.startTime;
  }

  public void setStartTime(Time startTime) {
    this.startTime = startTime;
  }

  public Time getEndTime() {
    return this.endTime;
  }

  public void setEndTime(Time endTime) {
    this.endTime = endTime;
  }

  public String getText() {
    return this.text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Subtitle clone() {
    return new Subtitle(startTime, endTime, text);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Subtitle subtitle = (Subtitle) obj;
    return startTime.equals(subtitle.startTime)
        && endTime.equals(subtitle.endTime)
        && text.equals(subtitle.text);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeParcelable(startTime, flags);
    parcel.writeParcelable(endTime, flags);
    parcel.writeString(text);
  }

  public static final Creator<Subtitle> CREATOR =
      new Creator<Subtitle>() {
        @Override
        public Subtitle createFromParcel(Parcel parcel) {
          return new Subtitle(parcel);
        }

        @Override
        public Subtitle[] newArray(int size) {
          return new Subtitle[size];
        }
      };
}
