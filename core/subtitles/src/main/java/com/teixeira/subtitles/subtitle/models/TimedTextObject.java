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

import com.teixeira.subtitles.subtitle.exceptions.ParsingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Felipe Teixeira
 */
public class TimedTextObject {

  private TimedTextInfo timedTextInfo;
  private List<Subtitle> subtitles;
  private List<SyntaxError> errors;

  public TimedTextObject(TimedTextInfo timedTextInfo) {
    this.timedTextInfo = timedTextInfo;

    this.subtitles = new ArrayList<>();
    this.errors = new ArrayList<>();
  }

  public TimedTextInfo getTimedTextInfo() {
    return this.timedTextInfo;
  }

  public void setTimedTextInfo(TimedTextInfo timedTextInfo) {
    this.timedTextInfo = timedTextInfo;
  }

  public List<Subtitle> getSubtitles() {
    return this.subtitles;
  }

  public void setSubtitles(List<Subtitle> subtitles) {
    this.subtitles = subtitles;
  }

  public List<SyntaxError> getErrors() {
    return this.errors;
  }

  public void setErrors(List<SyntaxError> errors) {
    this.errors = errors;
  }

  public String toFile() {
    return timedTextInfo.getFormat().toFile(this);
  }
}
