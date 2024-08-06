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

package com.teixeira.subtitles.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.VideoView;
import com.blankj.utilcode.util.ThreadUtils;

public class SubVideoView extends VideoView {

  private static final Handler mainHandler = ThreadUtils.getMainHandler();

  private OnEveryMilliSecondListener onEveryMilliSecondListener;
  private Runnable onEveryMilliSecondCallback;

  public SubVideoView(Context context) {
    this(context, null);
  }

  public SubVideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SubVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    onEveryMilliSecondCallback = this::onEveryMilliSecond;
  }

  @Override
  public void start() {
    super.start();

    if (onEveryMilliSecondListener != null) {
      mainHandler.removeCallbacks(onEveryMilliSecondCallback);
      mainHandler.post(onEveryMilliSecondCallback);
    }
  }

  public void setOnEveryMilliSecondListener(OnEveryMilliSecondListener onEveryMilliSecondListener) {
    this.onEveryMilliSecondListener = onEveryMilliSecondListener;
  }

  private void onEveryMilliSecond() {

    if (onEveryMilliSecondListener != null) {
      onEveryMilliSecondListener.onEveryMilliSecond(getCurrentPosition());
    }

    if (isPlaying()) {
      mainHandler.removeCallbacks(onEveryMilliSecondCallback);
      mainHandler.postDelayed(onEveryMilliSecondCallback, 1L);
    }
  }

  public interface OnEveryMilliSecondListener {
    void onEveryMilliSecond(int currentVideoPosition);
  }
}
