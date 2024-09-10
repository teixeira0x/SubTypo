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

import android.net.Uri;
import android.text.TextUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.subtitle.formats.SubRipFormat;
import com.teixeira.subtitles.subtitle.formats.SubtitleFormat;
import com.teixeira.subtitles.subtitle.models.Subtitle;
import com.teixeira.subtitles.utils.FileUtil;
import com.teixeira.subtitles.utils.UriUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import kotlin.collections.CollectionsKt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handle project files and data.
 *
 * @author Felipe Teixeira
 */
public class ProjectRepository {

  public static final String PROJECT_CREATOR_VERSION = "1.0";

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

    CollectionsKt.sort(projects);

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

      JSONObject configObj = new JSONObject(FileUtil.readFile(configFile.getPath()));
      if (!isValidConfigObj(configObj)) {
        return null;
      }

      String videoPath;
      if (configObj.has("video_path")) {
        // Old
        videoPath = configObj.getString("video_path");
      } else {
        // New
        videoPath = configObj.getString("videoPath");
      }

      String version;
      if (configObj.has("version") && configObj.get("version") instanceof String) {
        version = configObj.getString("version");
      } else {
        version = PROJECT_CREATOR_VERSION;
      }

      return new Project(
          projectDir.getName(),
          projectDir.getPath(),
          configObj.getString("name"),
          videoPath,
          version);
    } catch (JSONException jsone) {
      return null;
    }
  }

  /**
   * Create a new project.
   *
   * @param name The name of the project.
   * @param videoUri The uri of the project's video file.
   * @return The project created.
   */
  public static Project createProject(String name, Uri videoUri) {
    try {
      String projectId = generateProjectId();
      File projectDir = new File(PROJECTS_DIR, projectId);
      if (!projectDir.exists()) {
        projectDir.mkdirs();
      }

      File videoFile = UriUtils.getUri2File(videoUri);

      writeConfigFile(projectDir.getPath(), name, videoFile, PROJECT_CREATOR_VERSION);
      writeSubtitleDataFile(projectDir.getAbsolutePath(), List.of(new Subtitle("subtitle", new SubRipFormat(), new ArrayList<>())));
      return new Project(
          projectId, projectDir.getPath(), name, videoFile.getPath(), PROJECT_CREATOR_VERSION);
    } catch (JSONException jsone) {
      jsone.printStackTrace();
      return null;
    }
  }

  /**
   * Update an existing project .
   *
   * @param projectId The id of the project to update.
   * @param name The name of the project.
   * @param videoUri The uri of the project's video file.
   */
  public static Project updateProject(String projectId, String name, Uri videoUri) {
    try {
      File projectDir = new File(PROJECTS_DIR, projectId);
      if (!projectDir.exists()) {
        return null;
      }

      File videoFile = UriUtils.getUri2File(videoUri);

      writeConfigFile(projectDir.getPath(), name, videoFile, PROJECT_CREATOR_VERSION);
      return findProject(projectDir);
    } catch (JSONException jsone) {
      jsone.printStackTrace();
      return null;
    }
  }

  /**
   * Writes or rewrites the given project configuration file.
   *
   * @param projectDir The project directory to write the configuration file.
   * @param project The project to write the configuration file.
   */
  public static void writeConfigFile(
      String projectPath, String name, File videoFile, String creatorVersion) throws JSONException {
    JSONObject configJson = new JSONObject();
    configJson.put("name", name);
    configJson.put("videoPath", videoFile.getPath());
    configJson.put("version", creatorVersion);

    try (FileOutputStream outputStream = new FileOutputStream(projectPath + "/config.json")) {
      outputStream.write(configJson.toString().getBytes());
      outputStream.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
   * Writes or rewrites the subtitle data file of the given project.
   *
   * @param projectDir The project directory to write the subtitle data file to.
   * @param project The project to write the subtitle data file.
   */
  public static void writeSubtitleDataFile(String projectDir, List<Subtitle> subtitles)
      throws JSONException {
    JSONArray subtitleArr = new JSONArray();
    for (Subtitle subtitle : subtitles) {
      JSONObject subtitleObj = new JSONObject();
      subtitleObj.put("name", subtitle.getName());
      subtitleObj.put("format", subtitle.getSubtitleFormat().getExtension());
      subtitleObj.put("content", subtitle.toText());
      subtitleArr.put(subtitleObj);
    }

    try (BufferedOutputStream outputStream =
        new BufferedOutputStream(new FileOutputStream(projectDir + "/subtitle.data"))) {
      outputStream.write(EncodeUtils.base64Encode(subtitleArr.toString()));
      outputStream.flush();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
   * Returns the list of subtitle files or creates them for the given project
   *
   * @param project The project to get the subtitle files.
   * @return The list of subtitle files.
   */
  public static List<Subtitle> getProjectSubtitles(Project project) throws JSONException {
    List<Subtitle> subtitles = new ArrayList<>();

    File subtitleDataFile = new File(project.getPath(), "subtitle.data");
    if (subtitleDataFile.exists() && !subtitleDataFile.isDirectory()) {

      String subtitleData =
          new String(EncodeUtils.base64Decode(FileUtil.readFile(subtitleDataFile.getPath())));

      JSONArray subtitleArr = new JSONArray(subtitleData);

      if (subtitleArr != null) {
        for (int i = 0; i < subtitleArr.length(); i++) {
          JSONObject subtitleObj = subtitleArr.getJSONObject(i);
          if (!isValidSubtitleJsonObj(subtitleObj)) {
            continue;
          }

          SubtitleFormat format =
              SubtitleFormat.Builder.from(subtitleObj.getString("format")).build();

          String name = subtitleObj.getString("name");
          String content = subtitleObj.getString("content");

          Subtitle subtitle = new Subtitle(name, format, format.parseText(content));

          subtitles.add(subtitle);
        }
      }
    }

    return subtitles;
  }

  /**
   * Go through the projects folder until you find the largest ID, after finding it, it returns the
   * found ID adding it.
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

    return String.valueOf(id += 1);
  }

  private static boolean isValidConfigObj(JSONObject configObj) throws JSONException {
    if (configObj == null) {
      return false;
    }

    if (!(configObj.has("name")
        && (configObj.has("videoPath") || configObj.has("video_path"))
        && configObj.has("version"))) {
      return false;
    }

    if (!(configObj.get("name") instanceof String
        && (configObj.get("videoPath") instanceof String
            || configObj.get("video_path") instanceof String)
        && configObj.get("version") instanceof String)) {
      return false;
    }

    return true;
  }

  private static boolean isValidSubtitleJsonObj(JSONObject subtitleJsonObj) throws JSONException {
    if (subtitleJsonObj == null) {
      return false;
    }

    if (!(subtitleJsonObj.has("name")
        && subtitleJsonObj.has("format")&& subtitleJsonObj.has("content"))) {
      return false;
    }

    if (!(subtitleJsonObj.get("name") instanceof String
        && subtitleJsonObj.get("format") instanceof String && subtitleJsonObj.has("content"))) {
      return false;
    }

    return true;
  }
}
