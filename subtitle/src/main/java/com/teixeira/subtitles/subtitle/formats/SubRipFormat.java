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

import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.teixeira.subtitles.subtitle.exceptions.ParsingException;
import com.teixeira.subtitles.subtitle.models.SyntaxError;
import com.teixeira.subtitles.subtitle.models.Subtitle;
import com.teixeira.subtitles.subtitle.models.TimedTextInfo;
import com.teixeira.subtitles.subtitle.models.TimedTextObject;
import com.teixeira.subtitles.subtitle.utils.TimeUtils;
import java.util.List;
import java.util.Objects;

/**
 * @author Felipe Teixeira
 */
public class SubRipFormat extends SubtitleFormat {

  public SubRipFormat() {
    super("SubRip", ".srt");
  }

  @Override
  public String toFile(@NonNull TimedTextObject timedTextObject) {
    Objects.requireNonNull(timedTextObject);

    List<Subtitle> subtitles = timedTextObject.getSubtitles();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < subtitles.size(); i++) {

      Subtitle subtitle = subtitles.get(i);
      String startTime = subtitle.getStartTime().getTime();
      String endTime = subtitle.getEndTime().getTime();
      String text = subtitle.getText();

      sb.append(String.valueOf(i + 1));
      sb.append("\n");
      sb.append(startTime).append(" --> ").append(endTime);
      sb.append("\n");
      sb.append(text);
      sb.append("\n\n");
    }

    return sb.toString().trim();
  }

  @Override
  public TimedTextObject parseFile(@NonNull TimedTextInfo timedTextInfo, @NonNull String content)
      throws ParsingException {
    Objects.requireNonNull(timedTextInfo);
    Objects.requireNonNull(content);

    TimedTextObject timedTextObject = new TimedTextObject(timedTextInfo);
    List<Subtitle> subtitles = timedTextObject.getSubtitles();
    List<SyntaxError> errors = timedTextObject.getErrors();
    String[] lines = content.split("\n");

    int captionNumber = 0;
    int lineIndex = 0;

    while (lineIndex < lines.length) {
      String line = lines[lineIndex].trim();
      if (TextUtils.isEmpty(line)) {
        lineIndex++;
        continue;
      }

      if (TextUtils.isDigitsOnly(line)) {
        int number = Integer.parseInt(line);
        if (captionNumber + 1 != number) {
          errors.add(
              new SyntaxError(
                  "Found number: " + number + ", expected number: " + captionNumber + 1,
                  lineIndex));
          lineIndex++;
          continue;
        }

        lineIndex++;

        String timeCodesLine = lines[lineIndex].trim();
        if (TextUtils.isEmpty(timeCodesLine)) {
          errors.add(new SyntaxError("Time codes line not found", lineIndex));
          continue;
        }
        
        String[] times = timeCodesLine.split(" --> ");

        String startTime = times[0].trim();
        String endTime = times[1].trim();

        if (!TimeUtils.isValidTime(startTime.split(":"))
            || !TimeUtils.isValidTime(endTime.split(":"))) {
          errors.add(new SyntaxError("Incorrect time formatting", lineIndex));
          continue;
        }

        lineIndex++;

        String text = "";
        while (lineIndex < lines.length) {
          line = lines[lineIndex].trim();
          if (TextUtils.isEmpty(line)) {
            break;
          }
          text += line + "\n";
          lineIndex++;
        }

        subtitles.add(
            new Subtitle(
                TimeUtils.getMilliseconds(startTime),
                TimeUtils.getMilliseconds(endTime),
                text.trim()));

        captionNumber++;
        lineIndex++;
      } else {
        errors.add(new SyntaxError("Caption number not found", lineIndex));
        break;
      }
    }
    return timedTextObject;
  }
}
