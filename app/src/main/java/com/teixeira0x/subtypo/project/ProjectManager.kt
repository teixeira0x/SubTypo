package com.teixeira0x.subtypo.project

import androidx.media3.common.Format
import com.teixeira0x.subtypo.models.Project
import com.teixeira0x.subtypo.project.internal.ProjectManagerImpl

interface ProjectManager {

  companion object {

    private var sInstance: ProjectManager? = null

    @JvmStatic
    fun getInstance(): ProjectManager {
      return sInstance
        ?: synchronized(this) {
          sInstance ?: ProjectManagerImpl().also { sInstance = it }
        }
    }
  }

  /** Open project */
  val project: Project?

  /** Open project video format */
  val videoFormat: Format?

  /**
   * Configure and open the provided project.
   *
   * @param The project to open.
   */
  fun openProject(project: Project)

  /**
   * Update current video information.
   *
   * @param videoFormat The player's video format.
   */
  fun updateVideoFormat(videoFormat: Format?)

  /** destroy */
  fun destroy()
}
