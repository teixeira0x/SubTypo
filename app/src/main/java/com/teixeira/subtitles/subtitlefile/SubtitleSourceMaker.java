package com.teixeira.subtitles.subtitlefile;

import com.teixeira.subtitles.models.Subtitle;
import java.util.List;

public class SubtitleSourceMaker {

  public String makeSubRipSource(List<Subtitle> subtitles) {
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
}
