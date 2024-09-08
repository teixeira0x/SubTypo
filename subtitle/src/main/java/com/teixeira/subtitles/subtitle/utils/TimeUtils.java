package com.teixeira.subtitles.subtitle.utils;

/**
 * Contains functions for manipulating milliseconds and time strings.
 *
 * @author Felipe Teixeira
 */
public class TimeUtils {

  /**
   * Converts the given milliseconds to a String in the format hh:mm:ss,SSS.
   *
   * @param milliseconds Milliseconds to convert to String.
   * @return The time String in the format hh:mm:ss,SSS.
   */
  public static String getFormattedTime(long milliseconds) {
    long hours = milliseconds / 3600000;
    long minutes = (milliseconds % 3600000) / 60000;
    long seconds = (milliseconds % 60000) / 1000;
    long millis = milliseconds % 1000;

    return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
  }

  /**
   * Converts the given hh:mm:ss,SSS format time string to milliseconds.
   *
   * @param time The time to be converted to milliseconds.
   * @return The milliseconds obtained from the time String.
   */
  public static long getMilliseconds(String time) {
    String[] timeParts = time.split(":");
    if (!isValidTime(timeParts)) {
      throw new IllegalArgumentException("Time format must be hh:mm:ss,SSS");
    }

    long hours = Long.parseLong(timeParts[0]);
    long minutes = Long.parseLong(timeParts[1]);

    String secondsAndMillis = timeParts[2];
    String[] secParts = secondsAndMillis.split(",");

    long seconds = Long.parseLong(secParts[0]);
    long milliseconds = Long.parseLong(secParts[1]);

    long totalMillis = (hours * 3600000) + (minutes * 60000) + (seconds * 1000) + milliseconds;

    return totalMillis;
  }

  /**
   * Checks if the time given in the format hh:mm:ss,SSS is valid.
   *
   * @param timeParts The parts of time divided by ":".
   * @return If the time provided is valid.
   */
  public static boolean isValidTime(String[] timeParts) {
    if (timeParts == null || timeParts.length != 3) {
      return false;
    }

    String secondsAndMillis = timeParts[2];
    String[] secParts = secondsAndMillis.split(",");
    if (secParts == null || secParts.length != 2) {
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

  private TimeUtils() {}
}
