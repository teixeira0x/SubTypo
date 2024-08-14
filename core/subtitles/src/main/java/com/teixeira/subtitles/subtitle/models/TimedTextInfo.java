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

package com.teixeira.subtitles.subtitle.models;

import com.teixeira.subtitles.subtitle.formats.SubtitleFormat;

/**
 * @author Felipe Teixeira
 */
public class TimedTextInfo {

  private SubtitleFormat format;
  private String name;
  private String language;

  public TimedTextInfo(SubtitleFormat format, String name, String language) {
    this.format = format;
    this.name = name;
    this.language = language;
  }

  public SubtitleFormat getFormat() {
    return this.format;
  }

  public void setFormat(SubtitleFormat format) {
    this.format = format;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLanguage() {
    return this.language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
