package com.teixeira.subtitles.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import androidx.annotation.Nullable;
import java.util.concurrent.TimeUnit;

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

  public static String getTime(long ms) {
    long hours = TimeUnit.MILLISECONDS.toHours(ms);
    long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(hours);
    long seconds =
        TimeUnit.MILLISECONDS.toSeconds(ms)
            - TimeUnit.HOURS.toSeconds(hours)
            - TimeUnit.MINUTES.toSeconds(minutes);
    long milliseconds =
        ms
            - TimeUnit.HOURS.toMillis(hours)
            - TimeUnit.MINUTES.toMillis(minutes)
            - TimeUnit.SECONDS.toMillis(seconds);

    return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
  }

  public static long getMilliSeconds(String time) {
    String[] parts = time.split(":");
    if (!isValidTime(parts)) {
      throw new IllegalArgumentException("Time format must be hh:mm:ss,SSS");
    }

    long hours = Long.parseLong(parts[0]);
    long minutes = Long.parseLong(parts[1]);

    String secondsAndMillis = parts[2];
    String[] secParts = secondsAndMillis.split(",");
    long seconds = Long.parseLong(secParts[0]);
    long milliseconds = Long.parseLong(secParts[1]);

    long totalMillis =
        TimeUnit.HOURS.toMillis(hours)
            + TimeUnit.MINUTES.toMillis(minutes)
            + TimeUnit.SECONDS.toMillis(seconds)
            + milliseconds;

    return totalMillis;
  }

  public static boolean isValidTime(String[] timeParts) {
    if (timeParts.length != 3) {
      return false;
    }

    String secondsAndMillis = timeParts[2];
    String[] secParts = secondsAndMillis.split(",");
    if (secParts.length != 2) {
      return false;
    }

    try {
      int hours = Integer.parseInt(timeParts[0]);
      int minutes = Integer.parseInt(timeParts[1]);
      int seconds = Integer.parseInt(secParts[0]);
      int millis = Integer.parseInt(secParts[1]);

      if (hours < 0
          || hours > 99
          || minutes < 0
          || minutes > 59
          || seconds < 0
          || seconds > 59
          || millis < 0
          || millis > 999) {
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }

    return true;
  }

  private VideoUtils() {}
}
