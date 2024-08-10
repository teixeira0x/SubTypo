package com.teixeira.subtitles.subtitle.format;

import androidx.annotation.NonNull;
import com.teixeira.subtitles.models.Subtitle;
import java.util.List;

public abstract class SubtitleFormat {

  public static final SubtitleFormat FORMAT_SUBRIP = new SubRipFormat();

  public static SubtitleFormat getExtensionFormat(String extension) {
    if (extension.equals(".srt")) {
      return FORMAT_SUBRIP;
    }

    return null;
  }

  private String extension;

  /**
   * Creates a new subtitle format object with the specified extension.
   *
   * @param extension Subtitle format file extension.
   */
  public SubtitleFormat(String extension) {
    this.extension = extension;
  }

  /**
   * Converts the specified subtitle list to text.
   *
   * @param subtitles The list to convert to text.
   */
  public abstract String toText(@NonNull List<Subtitle> subtitles) throws Exception;

  /**
   * Convert text to a list of subtitles
   *
   * @param content The text to convert.
   */
  public abstract List<Subtitle> toList(@NonNull String content) throws Exception;

  public String getExtension() {
    return this.extension;
  }
}
