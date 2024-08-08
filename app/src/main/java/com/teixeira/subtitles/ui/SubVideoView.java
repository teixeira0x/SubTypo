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
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import com.blankj.utilcode.util.ThreadUtils;

public class SubVideoView extends PlayerView {

  private static final Handler mainHandler = ThreadUtils.getMainHandler();

  private Player.Listener listener;
  private ExoPlayer player;

  public SubVideoView(Context context) {
    this(context, null);
  }

  public SubVideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SubVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    player = new ExoPlayer.Builder(context).build();
    setUseController(false);
    setPlayer(player);
  }

  public void setPlayerListener(Player.Listener listener) {
    this.listener = listener;

    if (listener != null) {
      player.addListener(listener);
    }
  }

  public void setVideoPath(String path) {
    player.setMediaItem(MediaItem.fromUri(path));
    player.prepare();
  }

  public void setPlaying(boolean isPlaying) {
    if (isPlaying) {
      play();
    } else {
      pause();
    }
  }

  public void play() {
    player.play();
  }

  public void pause() {
    player.pause();
  }

  public void stop() {
    player.stop();
  }

  public void seekBackward() {
    player.seekBack();
  }

  public void seekFoward() {
    player.seekForward();
  }

  public void seekTo(long position) {
    player.seekTo(position);
  }

  public long getCurrentPosition() {
    return player.getCurrentPosition();
  }

  public void release() {
    if (listener != null) {
      player.removeListener(listener);
    }
    player.release();
    listener = null;
    player = null;
  }
}
