package com.teixeira.subtitles.subtitle.exceptions;

public class SubtitleTimeFormatException extends SubtitleException {

  public SubtitleTimeFormatException(String error) {
    super(error);
  }

  public SubtitleTimeFormatException(String error, Exception cause) {
    super(error, cause);
  }
}
