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

package com.teixeira0x.subtypo.data.repository.project

import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.domain.model.ProjectData
import com.teixeira0x.subtypo.domain.repository.project.ProjectRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ProjectRepositoryImpl
@Inject
constructor(private val localDataSource: LocalProjectDataSource) :
  ProjectRepository {

  override fun getAll(): Flow<List<Project>> {
    return localDataSource.getAll()
  }

  override fun getProject(id: Long): Flow<Project?> {
    return localDataSource.getProject(id)
  }

  override fun getProjectData(id: Long): Flow<ProjectData?> {
    return localDataSource.getProjectData(id)
  }

  override suspend fun insertProject(project: Project): Long {
    return localDataSource.insertProject(project)
  }

  override suspend fun updateProject(project: Project) {
    localDataSource.updateProject(project)
  }

  override suspend fun removeProject(id: Long) {
    localDataSource.removeProject(id)
  }
}
