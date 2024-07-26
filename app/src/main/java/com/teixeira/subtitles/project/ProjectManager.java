package com.teixeira.subtitles.project;

import com.teixeira.subtitles.models.Project;

public class ProjectManager {

  private static volatile ProjectManager sInstance;

  public static ProjectManager getInstance() {
    if (sInstance == null) {
      synchronized (ProjectManager.class) {
        if (sInstance == null) sInstance = new ProjectManager();
      }
    }
    return sInstance;
  }

  private ProjectManager() {}

  private Project project = null;

  public Project setupProject(Project project) {
    this.project = project;

    return project;
  }

  public Project getProject() {
    return this.project;
  }

  public void destroy() {
    this.project = null;
  }
}
