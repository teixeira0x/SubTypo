package com.teixeira0x.subtypo.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import com.teixeira0x.subtypo.App;
import java.io.File;
import java.io.IOException;

public class VideoUtils {

  public static Bitmap getVideoThumbnail(String path) {
    return getVideoThumbnail(Uri.fromFile(new File(path)));
  }

  public static Bitmap getVideoThumbnail(Uri uri) {
    Bitmap thumbnail = null;
    var retriever = new MediaMetadataRetriever();

    try {
      retriever.setDataSource(App.getInstance(), uri);
      thumbnail = retriever.getFrameAtTime();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        retriever.release();
      } catch (IOException ioe) {
        // Nothing
      }
    }

    return thumbnail;
  }

  private VideoUtils() {}
}
