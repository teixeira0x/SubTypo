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

package com.teixeira0x.subtypo.ui;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.Player.Events;
import androidx.media3.ui.AspectRatioFrameLayout;
import com.teixeira0x.subtypo.R;
import java.util.Objects;

/**
 * A simple implementation for video viewer using {@link ExoPlayer}.
 *
 * @author Felipe Teixeira
 */
public class VideoView extends FrameLayout {

  private static final float NORMAL_ASPECT_RATIO = 1.5f;

  private static final long DEFAULT_SEEK_BACK_MS = 5_000L;
  private static final long DEFAULT_SEEK_FOWARD_MS = 5_000L;

  private final ComponentListener componentListener;
  private final AspectRatioFrameLayout contentFrame;
  private final SurfaceView surfaceView;

  private ExoPlayer player;

  public VideoView(Context context) {
    this(context, null);
  }

  public VideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    contentFrame = new AspectRatioFrameLayout(context);
    contentFrame.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
    addView(contentFrame);

    surfaceView = new SurfaceView(context);
    contentFrame.addView(surfaceView, 0);

    componentListener = new ComponentListener();

    player =
        new ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(DEFAULT_SEEK_BACK_MS)
            .setSeekForwardIncrementMs(DEFAULT_SEEK_FOWARD_MS)
            .build();

    player.addListener(componentListener);
    player.setVideoSurfaceView(surfaceView);
  }

  public void setUri(Uri uri) {
    setMedia(MediaItem.fromUri(uri));
  }

  public void setUri(String uri) {
    setMedia(MediaItem.fromUri(uri));
  }

  public void setMedia(@NonNull MediaItem media) {
    Objects.requireNonNull(media);
    prepareMedia(media);
  }

  public void addListener(@NonNull Player.Listener listener) {
    Objects.requireNonNull(listener);
    player.addListener(listener);
  }

  public void setPlaying(boolean play) {
    if (play) play();
    else pause();
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

  public void seekTo(long position) {
    player.seekTo(position);
  }

  public void seekBackward() {
    player.seekBack();
  }

  public void seekFoward() {
    player.seekForward();
  }

  public long getCurrentPosition() {
    return player.getCurrentPosition();
  }

  public Format getVideoFormat() {
    return player.getVideoFormat();
  }

  public ExoPlayer getPlayer() {
    return player;
  }

  public void release() {
    player.release();
    player = null;
  }

  private void prepareMedia(MediaItem media) {
    player.clearMediaItems();
    player.setMediaItem(media);
    player.prepare();
  }

  private final class ComponentListener implements Player.Listener {

    @Override
    public void onEvents(Player player, Events events) {
      
    }


    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {

      int width = videoSize.width;
      int height = videoSize.height;
      float videoAspectRatio =
          (height == 0 || width == 0) ? 0 : (width * videoSize.pixelWidthHeightRatio) / height;

      contentFrame.setAspectRatio(videoAspectRatio);

      if (videoAspectRatio > NORMAL_ASPECT_RATIO) {
        getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
      } else {
        Resources resources = getContext().getResources();
        // Set maximum view size if video is not in 16:9 format.
        getLayoutParams().height = (int) resources.getDimension(R.dimen.video_view_height_max);
      }
    }
  }
}
