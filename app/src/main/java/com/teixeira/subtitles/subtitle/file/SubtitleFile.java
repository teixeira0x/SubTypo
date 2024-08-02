package com.teixeira.subtitles.subtitle.file;

import com.teixeira.subtitles.subtitle.format.SubtitleFormat;

public class SubtitleFile {
  private SubtitleFormat subtitleFormat;
  private String fileName;

  public SubtitleFile(SubtitleFormat subtitleFormat, String fileName) {
    this.subtitleFormat = subtitleFormat;
    this.fileName = fileName;
  }

  public SubtitleFormat getSubtitleFormat() {
    return this.subtitleFormat;
  }

  public void setSubtitleFormat(SubtitleFormat subtitleFormat) {
    this.subtitleFormat = subtitleFormat;
  }

  public String getNameWithExtension() {
    return this.fileName + this.subtitleFormat.getExtension();
  }

  public String getName() {
    return this.fileName;
  }

  public void setName(String fileName) {
    this.fileName = fileName;
  }
}
