package com.teixeira.subtitles.subtitle.file;

import android.os.Parcel;
import android.os.Parcelable;
import com.teixeira.subtitles.project.ProjectManager;
import com.teixeira.subtitles.subtitle.format.SubtitleFormat;

public class SubtitleFile implements Parcelable {
  private SubtitleFormat subtitleFormat;
  private String name, lang, content;

  public SubtitleFile(SubtitleFormat subtitleFormat, String name, String lang, String content) {
    this.subtitleFormat = subtitleFormat;
    this.name = name;
    this.lang = lang;
    this.content = content;
  }

  public SubtitleFormat getSubtitleFormat() {
    return this.subtitleFormat;
  }

  public void setSubtitleFormat(SubtitleFormat subtitleFormat) {
    this.subtitleFormat = subtitleFormat;
  }

  public String getPath() {
    return ProjectManager.getInstance().getProject().getProjectPath()
        + "/"
        + getNameWithExtension();
  }

  public String getNameWithExtension() {
    return this.name + this.subtitleFormat.getExtension();
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLang() {
    return this.lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getContent() {
    return this.content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeString(subtitleFormat.getExtension());
    parcel.writeString(name);
    parcel.writeString(lang);
    parcel.writeString(content);
  }

  public static final Creator<SubtitleFile> CREATOR =
      new Creator<>() {

        @Override
        public SubtitleFile createFromParcel(Parcel parcel) {
          return new SubtitleFile(
              SubtitleFormat.getExtensionFormat(parcel.readString()),
              parcel.readString(),
              parcel.readString(),
              parcel.readString());
        }

        @Override
        public SubtitleFile[] newArray(int lenght) {
          return new SubtitleFile[lenght];
        }
      };
}
