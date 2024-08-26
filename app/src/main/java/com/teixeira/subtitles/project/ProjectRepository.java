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
import com.blankj.utilcode.util.UriUtils;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.subtitle.formats.SubtitleFormat;
import com.teixeira.subtitles.subtitle.models.TimedTextInfo;
import com.teixeira.subtitles.subtitle.models.TimedTextObject;
import com.teixeira.subtitles.utils.FileUtil;
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

      File videoFile = UriUtils.uri2File(videoUri);

      writeConfigFile(projectDir.getPath(), name, videoFile, PROJECT_CREATOR_VERSION);
      writeSubtitleDataFile(
          projectDir.getAbsolutePath(),
          List.of(new TimedTextObject(new TimedTextInfo(SubtitleFormat.SUBRIP, "subtitle", "en"))));
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
  public static void updateProject(String projectId, String name, Uri videoUri) {
    try {
      File projectDir = new File(PROJECTS_DIR, projectId);
      if (!projectDir.exists()) {
        return;
      }

      File videoFile = UriUtils.uri2File(videoUri);

      writeConfigFile(projectDir.getPath(), name, videoFile, PROJECT_CREATOR_VERSION);
    } catch (JSONException jsone) {
      jsone.printStackTrace();
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
  public static void writeSubtitleDataFile(
      String projectDir, List<TimedTextObject> timedTextObjects) throws JSONException {
    JSONArray subtitleFilesArr = new JSONArray();
    for (TimedTextObject timedTextObject : timedTextObjects) {

      TimedTextInfo timedTextInfo = timedTextObject.getTimedTextInfo();

      JSONObject subtitleFileObj = new JSONObject();
      subtitleFileObj.put("name", timedTextInfo.getName());
      subtitleFileObj.put("lang", timedTextInfo.getLanguage());
      subtitleFileObj.put("format", timedTextInfo.getFormat().getExtension());
      subtitleFileObj.put("content", timedTextObject.toFile());
      subtitleFilesArr.put(subtitleFileObj);
    }

    try (BufferedOutputStream outputStream =
        new BufferedOutputStream(new FileOutputStream(projectDir + "/subtitle.data"))) {
      outputStream.write(EncodeUtils.base64Encode(subtitleFilesArr.toString()));
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
  public static List<TimedTextObject> getProjectTimedTextObjects(Project project)
      throws JSONException {
    List<TimedTextObject> timedTextObjects = new ArrayList<>();

    File subtitleDataFile = new File(project.getPath(), "subtitle.data");

    if (subtitleDataFile.exists() && !subtitleDataFile.isDirectory()) {

      String subtitleData =
          new String(EncodeUtils.base64Decode(FileUtil.readFile(subtitleDataFile.getPath())));

      JSONArray timedTextObjArr = new JSONArray(subtitleData);

      if (timedTextObjArr != null) {

        for (int i = 0; i < timedTextObjArr.length(); i++) {
          JSONObject timedTextJsonObj = timedTextObjArr.getJSONObject(i);
          if (!isValidTimedTextJsonObj(timedTextJsonObj)) {
            continue;
          }

          SubtitleFormat format =
              SubtitleFormat.getExtensionFormat(timedTextJsonObj.getString("format"));
          String name = timedTextJsonObj.getString("name");
          String language = timedTextJsonObj.getString("lang");
          String content = timedTextJsonObj.getString("content");

          TimedTextInfo timedTextInfo = new TimedTextInfo(format, name, language);

          try {
            TimedTextObject timedTextObject =
                timedTextInfo.getFormat().parseFile(timedTextInfo, content);

            timedTextObjects.add(timedTextObject);
          } catch (Exception e) {
          }
        }
      }
    }

    return timedTextObjects;
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

  private static boolean isValidTimedTextJsonObj(JSONObject timedTextJsonObj) throws JSONException {
    if (timedTextJsonObj == null) {
      return false;
    }

    if (!(timedTextJsonObj.has("name")
        && timedTextJsonObj.has("lang")
        && timedTextJsonObj.has("format"))) {
      return false;
    }

    if (!(timedTextJsonObj.get("name") instanceof String
        && timedTextJsonObj.get("lang") instanceof String
        && timedTextJsonObj.get("format") instanceof String)) {
      return false;
    }

    return true;
  }
}
