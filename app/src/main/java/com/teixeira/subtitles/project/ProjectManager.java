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

import com.teixeira.subtitles.models.Project;

/**
 * Class to manipulate project data.
 *
 * @author Felipe Teixeira
 */
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

  private Project project = null;

  private ProjectManager() {}

  /**
   * Configure and open the provided project.
   *
   * @param The project to open.
   */
  public void openProject(Project project) {
    this.project = project;
  }

  /**
   * Get Open Project Instance.
   *
   * @return The open project instance.
   */
  public Project getProject() {
    return this.project;
  }

  /** Destroy and close the open project. */
  public void destroy() {
    this.project = null;
  }
}
