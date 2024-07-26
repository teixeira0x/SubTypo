package com.teixeira.subtitles.subtitlefile;

import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.utils.VideoUtils;
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

  public String makeTimedTextSource(List<Subtitle> subtitles) {
    StringBuilder sb = new StringBuilder();

    sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
    sb.append("<timedtext format=\"3\">\n");
    sb.append("<body>\n");

    for (int i = 0; i < subtitles.size(); i++) {

      Subtitle subtitle = subtitles.get(i);

      long startTime = VideoUtils.getMilliSeconds(subtitle.getStartTime());
      long endTime = VideoUtils.getMilliSeconds(subtitle.getEndTime());

      sb.append(
          "<p t=\""
              + startTime
              + "\" d=\""
              + (endTime - startTime)
              + "\">"
              + subtitle.getText()
              + "</p>");
      sb.append("\n");
    }

    sb.append("</body>\n");
    sb.append("</timedtext>");

    return sb.toString().trim();
  }
}
