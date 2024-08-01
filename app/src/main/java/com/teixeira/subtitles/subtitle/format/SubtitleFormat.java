package com.teixeira.subtitles.subtitle.file;

import androidx.annotation.NonNull;
import com.teixeira.subtitles.models.Subtitle;
import java.util.List;

public abstract class SubtitleFormat {

  public static final SubtitleFormat FORMAT_SUBRIP = new SubRipFormat();

  private String extension;

  public SubtitleFormat(String extension) {
    this.extension = extension;
  }

  public abstract String toText(@NonNull List<Subtitle> subtitles) throws Exception;

  public abstract List<Subtitle> toList(@NonNull String fileContent) throws Exception;

  public String getExtension() {
    return this.extension;
  }
}
