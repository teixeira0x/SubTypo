/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira.subtitles.project;

import static com.teixeira.subtitles.utils.FileUtil.PROJECTS_DIR;

import android.text.TextUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.subtitle.file.SubtitleFile;
import com.teixeira.subtitles.subtitle.format.SubtitleFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProjectRepository {

  /**
   * Browse through the projects folder to find projects and add them to a list.
   *
   * @return The list of projects found.
   */
  public static List<Project> fetchProjects() {
    List<Project> projects = new ArrayList<>();

    File projectsDir = PROJECTS_DIR;
    if (!projectsDir.exists()) {
      projectsDir.mkdirs();
    }

    File[] projectsDirs = projectsDir.listFiles((f, unused) -> f.isDirectory());
    if (projectsDirs == null) {
      return projects;
    }

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

  /**
   * Tries to find project data files in the given directory if found returns a project otherwise
   * returns null.
   *
   * @param projectDir Directory to try to find project data files.
   * @return Project found or null.
   */
  private static Project findProject(File projectDir) {
    try {
      if (!TextUtils.isDigitsOnly(projectDir.getName()) || !projectDir.isDirectory()) {
        return null;
      }

      File configFile = new File(projectDir, "config.json");
      if (!configFile.exists() || configFile.isDirectory()) {
        return null;
      }

      JSONObject configObj = new JSONObject(FileIOUtils.readFile2String(configFile));
      if (!isValidConfigObj(configObj)) {
        return null;
      }

      Project project =
          new Project(
              projectDir.getName(), configObj.getString("name"), configObj.getString("video_path"));

      return project;
    } catch (JSONException jsone) {
      return null;
    }
  }

  /**
   * Write a new project or rewrite an existing project.
   *
   * @param The project to write or rewrite.
   * @return The same project provided for writing.
   */
  public static Project writeProject(Project project) {
    try {
      File projectDir = new File(PROJECTS_DIR, getProjectId(project));
      if (!projectDir.exists()) {
        projectDir.mkdirs();
      }
      writeConfigFile(projectDir, project);
    } catch (JSONException jsone) {
      jsone.printStackTrace();
    }
    return project;
  }

  /**
   * Writes or rewrites the given project configuration file.
   *
   * @param projectDir The project directory to write the configuration file.
   * @param project The project to write the configuration file.
   */
  public static void writeConfigFile(File projectDir, Project project) throws JSONException {
    JSONObject configJson = new JSONObject();
    configJson.put("name", project.getName());
    configJson.put("video_path", project.getVideoPath());
    FileIOUtils.writeFileFromString(projectDir + "/config.json", configJson.toString());
  }

  /**
   * Writes or rewrites the subtitle data file of the given project.
   *
   * @param projectDir The project directory to write the subtitle data file to.
   * @param project The project to write the subtitle data file.
   */
  public static void writeSubtitleDataFile(Project project, List<SubtitleFile> subtitleFiles)
      throws JSONException {
    JSONArray subtitleFilesArr = new JSONArray();

    if (subtitleFiles.isEmpty()) {
      subtitleFiles.add(new SubtitleFile(SubtitleFormat.FORMAT_SUBRIP, "subtitle", "en", ""));
    }

    for (SubtitleFile subtitleFile : subtitleFiles) {
      JSONObject subtitleFileObj = new JSONObject();
      subtitleFileObj.put("name", subtitleFile.getName());
      subtitleFileObj.put("lang", subtitleFile.getLang());
      subtitleFileObj.put("format", subtitleFile.getSubtitleFormat().getExtension());
      subtitleFileObj.put("content", subtitleFile.getContent());
      subtitleFilesArr.put(subtitleFileObj);
    }

    FileIOUtils.writeFileFromString(
        project.getProjectPath() + "/subtitle.data",
        EncodeUtils.base64Encode2String(subtitleFilesArr.toString().getBytes()));
  }

  /**
   * Returns the list of subtitle files or creates them for the given project
   *
   * @param project The project to get the subtitle files.
   * @return The list of subtitle files.
   */
  public static List<SubtitleFile> getProjectSubtitleFiles(Project project) throws JSONException {
    List<SubtitleFile> subtitleFiles = new ArrayList<>();
    File subtitleDataFile = new File(project.getProjectPath(), "subtitle.data");
    if (subtitleDataFile.exists() && !subtitleDataFile.isDirectory()) {
      JSONArray subtitleFilesArr =
          new JSONArray(
              new String(EncodeUtils.base64Decode(FileIOUtils.readFile2String(subtitleDataFile))));

      if (subtitleFilesArr != null) {

        for (int i = 0; i < subtitleFilesArr.length(); i++) {
          JSONObject subtitleFileObj = subtitleFilesArr.getJSONObject(i);
          if (!isValidSubtitleFileObj(subtitleFileObj)) {
            continue;
          }

          subtitleFiles.add(
              new SubtitleFile(
                  SubtitleFormat.getExtensionFormat(subtitleFileObj.getString("format")),
                  subtitleFileObj.getString("name"),
                  subtitleFileObj.getString("lang"),
                  subtitleFileObj.getString("content")));
        }
      }
    }

    if (subtitleFiles.isEmpty()) {
      subtitleFiles.add(new SubtitleFile(SubtitleFormat.FORMAT_SUBRIP, "subtitle", "en", ""));
    }

    return subtitleFiles;
  }

  /**
   * Get project ID, if the project ID is null it generates a new ID.
   *
   * @return The id exists or a new id generated.
   */
  private static String getProjectId(Project project) {
    String projectId = project.getProjectId();
    if (projectId == null) {
      projectId = generateProjectId();
      project.setProjectId(projectId);
    }
    return projectId;
  }

  /**
   * Checks existing project ids recursively until the highest numbered id is found.
   *
   * @return The new id.
   */
  private static String generateProjectId() {
    int id = 0;
    File[] dirs = PROJECTS_DIR.listFiles((f, unused) -> f.isDirectory());
    for (File dir : dirs) {
      if (!TextUtils.isDigitsOnly(dir.getName())) continue;
      id = Math.max(id, Integer.parseInt(dir.getName()));
    }

    return String.valueOf(id + 1);
  }

  private static boolean isValidConfigObj(JSONObject configObj) throws JSONException {
    return configObj != null
        && configObj.get("name") instanceof String
        && configObj.get("video_path") instanceof String;
  }

  private static boolean isValidSubtitleFileObj(JSONObject subtitleFileObj) throws JSONException {
    return subtitleFileObj != null
        && subtitleFileObj.get("name") instanceof String
        && subtitleFileObj.get("lang") instanceof String
        && subtitleFileObj.get("format") instanceof String;
  }
}
