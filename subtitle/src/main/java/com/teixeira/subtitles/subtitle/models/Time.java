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
import com.teixeira.subtitles.subtitle.utils.TimeUtils;

/**
 * @author Felipe Teixeira
 */
public class Time implements Parcelable {

  private long milliseconds;

  public Time(long milliseconds) {
    this.milliseconds = milliseconds;
  }

  public String getTime() {
    return TimeUtils.getTime(this.milliseconds);
  }

  public long getMilliseconds() {
    return this.milliseconds;
  }

  public void setMilliseconds(long milliseconds) {
    this.milliseconds = milliseconds;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeLong(milliseconds);
  }

  public static final Creator<Time> CREATOR =
      new Creator<>() {

        @Override
        public Time createFromParcel(Parcel parcel) {
          return new Time(parcel.readLong());
        }

        @Override
        public Time[] newArray(int length) {
          return new Time[length];
        }
      };
}
