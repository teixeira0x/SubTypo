package com.teixeira.subtitles.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import androidx.annotation.Nullable;
import com.teixeira.subtitles.App;

public class VideoUtils {

  @Nullable
  public static Bitmap getVideoThumbnail(String videoPath) {
    Bitmap thumbnail = null;
    MediaMetadataRetriever retriever = null;

    try {
      retriever = new MediaMetadataRetriever();
      retriever.setDataSource(videoPath);
      thumbnail = retriever.getFrameAtTime();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return thumbnail;
  }

  public static Bitmap getVideoThumbnailFromUri(Uri uri) {
    Bitmap thumbnail = null;
    MediaMetadataRetriever retriever = null;

    try {
      retriever = new MediaMetadataRetriever();
      retriever.setDataSource(App.getInstance(), uri);
      thumbnail = retriever.getFrameAtTime();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return thumbnail;
  }
  
  private VideoUtils() {}
}
