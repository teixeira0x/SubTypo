package com.teixeira.subtitles.subtitle.exceptions;

public class SubtitleException extends Exception {

  public SubtitleException(String message) {
    super(message);
  }

  public SubtitleException(String message, Exception cause) {
    super(message, cause);
  }
}
