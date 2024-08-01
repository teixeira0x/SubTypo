package com.teixeira.subtitles.subtitle.file;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.subtitle.exceptions.SubtitleException;
import com.teixeira.subtitles.subtitle.exceptions.SubtitleTimeFormatException;
import com.teixeira.subtitles.utils.VideoUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubRipFormat extends SubtitleFormat {

  protected SubRipFormat() {
    super("srt");
  }

  @Override
  public String toText(@NonNull List<Subtitle> subtitles) throws Exception {
    Objects.requireNonNull(subtitles);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < subtitles.size(); i++) {

      Subtitle subtitle = subtitles.get(i);

      sb.append(String.valueOf(i + 1));
      sb.append("\n");
      sb.append(subtitle.getStartTime());
      sb.append(" --> ");
      sb.append(subtitle.getEndTime());
      sb.append("\n");
      sb.append(subtitle.getText());
      sb.append("\n\n");
    }

    return sb.toString().trim();
  }

  @Override
  public List<Subtitle> toList(@NonNull String fileContent) throws Exception {
    Objects.requireNonNull(fileContent);

    List<Subtitle> subtitles = new ArrayList<>();
    String[] lines = fileContent.split("\n");

    int lastSubtitleNumber = 0;
    int lineIndex = 0;

    while (lineIndex < lines.length) {
      String line = lines[lineIndex].trim();
      if (TextUtils.isEmpty(line)) {
        lineIndex++;
        continue;
      }

      if (TextUtils.isDigitsOnly(line)) {
        int subtitleNumber = Integer.parseInt(line);
        if (lastSubtitleNumber + 1 != subtitleNumber) {
          throw new SubtitleException("Invalid subtitle number at line " + lineIndex);
        }

        lineIndex++;

        String timeLine = lines[lineIndex].trim();
        String[] times = parseTimes(timeLine);
        String startTime = times[0];
        String endTime = times[1];

        lineIndex++;

        StringBuilder textBuilder = new StringBuilder();
        while (lineIndex < lines.length) {

          String text = lines[lineIndex];
          if (TextUtils.isEmpty(text)) {
            String nextLine = lines[lineIndex+1];
            if (!TextUtils.isDigitsOnly(nextLine)) {
              throw new SubtitleException("The caption text cannot contain empty lines.");
            }
            break;
          }

          textBuilder.append(text).append("\n");
          lineIndex++;
        }

        subtitles.add(new Subtitle(startTime, endTime, textBuilder.toString().trim()));

        lastSubtitleNumber++;
        lineIndex++;
      }
    }
    return subtitles;
  }

  private String[] parseTimes(String timeLine) throws SubtitleTimeFormatException {
    try {
      String[] times = timeLine.split(" --> ");
      String startTime = times[0].trim();
      String endTime = times[1].trim();

      String[] startTimeParts = startTime.split(":");
      String[] endTimeParts = endTime.split(":");

      if (!VideoUtils.isValidTime(startTimeParts) || !VideoUtils.isValidTime(endTimeParts)) {
        throw new SubtitleTimeFormatException("Incorrect time formatting: " + timeLine);
      }

      return new String[] {startTime, endTime};
    } catch (Exception e) {
      throw new SubtitleTimeFormatException("Incorrect time formatting: " + timeLine, e);
    }
  }

  private boolean isDigitsOnly(char c) {
    return c >= '0' && c <= '9';
  }
}
