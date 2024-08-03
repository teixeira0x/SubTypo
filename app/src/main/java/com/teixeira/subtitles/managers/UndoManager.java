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

package com.teixeira.subtitles.managers;

import android.os.Parcel;
import android.os.Parcelable;
import com.teixeira.subtitles.models.Subtitle;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Felipe Teixeira
 */
public class UndoManager implements Parcelable {

  public static final Creator<UndoManager> CREATOR =
      new Creator<>() {

        @Override
        public UndoManager createFromParcel(Parcel parcel) {
          return new UndoManager(parcel);
        }

        @Override
        public UndoManager[] newArray(int size) {
          return new UndoManager[size];
        }
      };

  private List<StackItem> subtitlesStack;
  private boolean enabled;
  private int stackPointer;
  private int maxStackSize;

  public UndoManager(int maxStackSize) {
    this.subtitlesStack = new ArrayList<>();
    this.enabled = true;
    this.stackPointer = 0;
    this.maxStackSize = maxStackSize;
  }

  private UndoManager(Parcel parcel) {
    enabled = parcel.readBoolean();
    stackPointer = parcel.readInt();
    maxStackSize = parcel.readInt();
    subtitlesStack = new ArrayList<>();
    int count = parcel.readInt();
    for (int i = 0; i < count; i++) {
      subtitlesStack.add(parcel.readParcelable(UndoManager.class.getClassLoader()));
    }
  }

  public void pushStack(List<Subtitle> subtitles) {

    if (!enabled) return;

    while (stackPointer < subtitlesStack.size() - 1) {
      subtitlesStack.remove(subtitlesStack.size() - 1);
    }

    subtitlesStack.add(new StackItem(subtitles));
    if (subtitlesStack.size() == maxStackSize) {
      subtitlesStack.remove(0);
    }
    stackPointer = subtitlesStack.size() - 1;
  }

  public boolean canUndo() {
    return enabled && stackPointer > 0;
  }

  public boolean canRedo() {
    return enabled && stackPointer < subtitlesStack.size() - 1;
  }

  public List<Subtitle> undo() {
    if (canUndo()) {
      stackPointer--;
      return subtitlesStack.get(stackPointer).subtitles;
    }
    return null;
  }

  public List<Subtitle> redo() {
    if (canRedo()) {
      stackPointer++;
      return subtitlesStack.get(stackPointer).subtitles;
    }
    return null;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeBoolean(enabled);
    parcel.writeInt(stackPointer);
    parcel.writeInt(maxStackSize);
    parcel.writeInt(subtitlesStack.size());
    for (StackItem item : subtitlesStack) {
      parcel.writeParcelable(item, flags);
    }
  }

  public static class StackItem implements Parcelable {

    public static final Creator<StackItem> CREATOR =
        new Creator<>() {

          @Override
          public StackItem createFromParcel(Parcel parcel) {
            List<Subtitle> subtitles = new ArrayList<>();
            int count = parcel.readInt();
            for (int i = 0; i < count; i++) {
              subtitles.add(parcel.readParcelable(StackItem.class.getClassLoader()));
            }
            return new StackItem(subtitles);
          }

          @Override
          public StackItem[] newArray(int size) {
            return new StackItem[size];
          }
        };

    private List<Subtitle> subtitles;

    public StackItem(List<Subtitle> subtitles) {
      this.subtitles = new ArrayList<>(subtitles);
    }

    public List<Subtitle> getSubtitles() {
      return this.subtitles;
    }

    public void setSubtitles(List<Subtitle> subtitles) {
      this.subtitles = subtitles;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
      parcel.writeInt(subtitles.size());
      for (Subtitle subtitle : subtitles) {
        parcel.writeParcelable(subtitle, flags);
      }
    }
  }
}
