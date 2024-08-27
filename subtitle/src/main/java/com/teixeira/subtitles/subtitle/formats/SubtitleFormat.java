/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira.subtitles.subtitle.formats;

import androidx.annotation.NonNull;
import com.teixeira.subtitles.subtitle.exceptions.ParsingException;
import com.teixeira.subtitles.subtitle.models.TimedTextInfo;
import com.teixeira.subtitles.subtitle.models.TimedTextObject;

/**
 * @author Felipe Teixeira
 */
public abstract class SubtitleFormat {

  public static final SubtitleFormat SUBRIP = new SubRipFormat();

  public static SubtitleFormat getExtensionFormat(String extension) {
    if (extension.equals(".srt")) {
      return SUBRIP;
    }

    return null;
  }

  private String name;
  private String extension;

  /**
   * Creates a new subtitle format object with the specified extension.
   *
   * @param extension Subtitle format file extension.
   */
  public SubtitleFormat(String name, String extension) {
    this.name = name;
    this.extension = extension;
  }

  public abstract String toFile(@NonNull TimedTextObject timedTextObject);

  public abstract TimedTextObject parseFile(
      @NonNull TimedTextInfo timedTextInfo, @NonNull String content) throws ParsingException;

  public String getName() {
    return this.name;
  }

  public String getExtension() {
    return this.extension;
  }
}
