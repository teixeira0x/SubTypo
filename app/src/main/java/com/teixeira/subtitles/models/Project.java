package com.teixeira.subtitles.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.teixeira.subtitles.utils.FileUtil;

public class Project implements Parcelable, Comparable<Project> {

  private String projectId;
  private String name;
  private String videoPath;

  public Project(String name, String videoPath) {
    this(null, name, videoPath);
  }

  public Project(String projectId, String name, String videoPath) {
    this.projectId = projectId;
    this.name = name;
    this.videoPath = videoPath;
  }

  public Project(Parcel parcel) {
    this(parcel.readString(), parcel.readString(), parcel.readString());
  }

  public String getProjectId() {
    return this.projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVideoPath() {
    return this.videoPath;
  }

  public void setVideoPath(String videoPath) {
    this.videoPath = videoPath;
  }

  public String getProjectPath() {
    if (projectId == null) {
      throw new NullPointerException("Unable to get path of a project with null id!");
    }
    return FileUtil.PROJECTS_DIR + "/" + projectId;
  }

  @Override
  public int compareTo(Project project) {
    if (projectId == null || project.projectId == null) {
      throw new NullPointerException("Cannot compare projects with null id!");
    }
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
    parcel.writeString(name);
    parcel.writeString(videoPath);
  }

  public static final Creator<Project> CREATOR =
      new Creator<>() {

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
