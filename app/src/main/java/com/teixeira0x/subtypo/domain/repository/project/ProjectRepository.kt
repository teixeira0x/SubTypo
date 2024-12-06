package com.teixeira0x.subtypo.domain.repository.project

import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.domain.model.ProjectData
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {

  fun getAll(): Flow<List<Project>>

  fun getProject(id: Long): Flow<Project?>

  fun getProjectData(id: Long): Flow<ProjectData?>

  suspend fun insertProject(project: Project): Long

  suspend fun updateProject(project: Project)

  suspend fun removeProject(id: Long)
}
