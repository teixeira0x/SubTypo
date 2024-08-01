package com.teixeira.subtitles.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.blankj.utilcode.util.FileIOUtils;
import com.teixeira.subtitles.subtitle.file.SubtitleFormat;
import java.io.File;
import java.util.List;

public class Project implements Parcelable, Comparable<Project> {

  private String projectId;
  private String projectPath;
  private String videoPath;
  private String name;

  private SubtitleFormat subtitleFormat;

  public Project(String projectId, String projectPath, String videoPath, String name) {
    this.projectId = projectId;
    this.projectPath = projectPath;
    this.videoPath = videoPath;
    this.name = name;

    this.subtitleFormat = SubtitleFormat.FORMAT_SUBRIP;
  }

  public Project(Parcel parcel) {
    this(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString());
  }

  public String getProjectId() {
    return this.projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getProjectPath() {
    return this.projectPath;
  }

  public void setProjectPath(String projectPath) {
    this.projectPath = projectPath;
  }

  public String getVideoPath() {
    return this.videoPath;
  }

  public void setVideoPath(String videoPath) {
    this.videoPath = videoPath;
  }

  public String getSubtitleFile() {
    return projectPath + "/subtitle." + subtitleFormat.getExtension();
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Subtitle> getSubtitles() throws Exception {
    File subtitleFile = new File(getSubtitleFile());
    if (!subtitleFile.exists()) {
      subtitleFile.createNewFile();
    }
    return subtitleFormat.toList(FileIOUtils.readFile2String(getSubtitleFile()));
  }

  public SubtitleFormat getSubtitleFormat() {
    return this.subtitleFormat;
  }

  @Override
  public int compareTo(Project project) {
    if (Integer.parseInt(projectId) > Integer.parseInt(project.projectId)) {
      return -1;
    }
    return 0;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeString(projectId);
    parcel.writeString(projectPath);
    parcel.writeString(videoPath);
    parcel.writeString(name);
  }

  public static final Parcelable.Creator<Project> CREATOR =
      new Parcelable.Creator<Project>() {

        @Override
        public Project createFromParcel(Parcel parcel) {
          return new Project(parcel);
        }

        @Override
        public Project[] newArray(int size) {
          return new Project[size];
        }
      };
}
