package com.teixeira.subtitles.callbacks;

import com.teixeira.subtitles.models.Subtitle;

public interface SubtitleEditorCallbacks {

  void addSubtitle(Subtitle subtitle);

  void updateSubtitle(int index, Subtitle subtitle);
}
