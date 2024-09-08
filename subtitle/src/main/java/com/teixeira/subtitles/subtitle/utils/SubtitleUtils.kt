package com.teixeira.subtitles.subtitle.utils

import com.teixeira.subtitles.subtitle.models.Paragraph

fun List<Paragraph>.getParagraphsAt(milliseconds: Long): List<Paragraph> {
  return filter {
    milliseconds >= it.startTime.milliseconds && milliseconds <= it.endTime.milliseconds
  }
}
