package com.teixeira0x.subtypo.domain.repository.project

import com.teixeira0x.subtypo.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectDataSource {

  fun getAll(): Flow<List<Project>>

  fun getProject(id: Long): Flow<Project?>

  suspend fun insertProject(project: Project): Long

  suspend fun updateProject(project: Project)

  suspend fun removeProject(id: Long): Int
}
