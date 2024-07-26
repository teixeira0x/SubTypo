package com.teixeira.subtitles.project;

import android.text.TextUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.utils.Constants;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class ProjectRepository {

  public static String writeProject(String name, String videoPath) {
    return writeProject(generateProjectId(), name, videoPath);
  }

  public static String writeProject(String projectId, String name, String videoPath) {
    try {
      File projectDir = new File(Constants.PROJECTS_DIR_PATH, projectId);
      if (!projectDir.exists()) {
        projectDir.mkdirs();
      }
      writeConfigFile(projectDir, name, videoPath);
    } catch (JSONException jsone) {
      jsone.printStackTrace();
    }
    return projectId;
  }

  public static List<Project> fetchProjects() {
    List<Project> projects = new ArrayList<>();
    File projectsDir = new File(Constants.PROJECTS_DIR_PATH);
    if (!projectsDir.exists()) {
      projectsDir.mkdirs();
    }

    File[] projectsDirs = projectsDir.listFiles((f, unused) -> f.isDirectory());
    for (File projectDir : projectsDirs) {
      Project project = findProject(projectDir);
      if (project == null) {
        continue;
      }
      projects.add(project);
    }

    Collections.sort(projects);

    return projects;
  }

  private static Project findProject(File dir) {
    try {
      if (!TextUtils.isDigitsOnly(dir.getName()) || !dir.isDirectory()) {
        return null;
      }

      File configFile = new File(dir, "/config.json");
      if (!configFile.exists() || configFile.isDirectory()) {
        return null;
      }

      JSONObject configJson = new JSONObject(FileIOUtils.readFile2String(configFile));
      return new Project(
          dir.getName(),
          dir.getAbsolutePath(),
          configJson.getString("video_path"),
          configJson.getString("name"));
    } catch (JSONException jsone) {
      return null;
    }
  }

  private static void writeConfigFile(File projectDir, String name, String videoPath)
      throws JSONException {
    JSONObject json = new JSONObject();
    json.put("name", name);
    json.put("video_path", videoPath);
    FileIOUtils.writeFileFromString(projectDir + "/config.json", json.toString());
  }

  private static String generateProjectId() {
    int id = 0;
    File[] dirs = new File(Constants.PROJECTS_DIR_PATH).listFiles((f, unused) -> f.isDirectory());
    for (File dir : dirs) {
      if (!TextUtils.isDigitsOnly(dir.getName())) continue;
      id = Math.max(id, Integer.parseInt(dir.getName()));
    }

    return String.valueOf(id + 1);
  }
}
