package com.teixeira.subtypo.project.internal

import androidx.media3.common.Format
import com.teixeira.subtypo.models.Project
import com.teixeira.subtypo.project.ProjectManager

class ProjectManagerImpl : ProjectManager {

  private var _project: Project? = null
  private var _videoFormat: Format? = null

  override val project: Project?
    get() = _project

  override val videoFormat: Format?
    get() = _videoFormat

  override fun openProject(project: Project) {
    this._project = project
  }

  override fun updateVideoFormat(videoFormat: Format?) {
    this._videoFormat = videoFormat
  }

  override fun destroy() {
    this._project = null
    this._videoFormat = null
  }
}
