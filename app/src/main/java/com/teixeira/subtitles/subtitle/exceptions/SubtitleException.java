package com.teixeira.subtitles.subtitle.exceptions;

public class SubtitleException extends Exception {

  public SubtitleException(String error) {
    super(error);
  }

  public SubtitleException(String error, Exception cause) {
    super(error, cause);
  }
}
