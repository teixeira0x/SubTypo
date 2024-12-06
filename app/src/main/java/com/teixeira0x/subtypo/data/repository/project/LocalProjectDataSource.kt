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

import com.teixeira0x.subtypo.data.db.SubTypoDatabase
import com.teixeira0x.subtypo.data.mapper.ProjectDataMapper.toEntity
import com.teixeira0x.subtypo.data.mapper.ProjectDataMapper.toModel
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.domain.model.ProjectData
import com.teixeira0x.subtypo.domain.repository.project.ProjectDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalProjectDataSource
@Inject
constructor(private val db: SubTypoDatabase) : ProjectDataSource {

  private val projectDao = db.projectDao

  override fun getAll(): Flow<List<Project>> {
    return projectDao.getAll().map { projects -> projects.map { it.toModel() } }
  }

  override fun getProject(id: Long): Flow<Project?> {
    return projectDao.findById(id).map { it?.toModel() }
  }

  override fun getProjectData(id: Long): Flow<ProjectData?> {
    return projectDao.getProjectData(id).map { it?.toModel() }
  }

  override suspend fun insertProject(project: Project): Long {
    return projectDao.insert(project.toEntity())
  }

  override suspend fun updateProject(project: Project) {
    projectDao.update(project.toEntity())
  }

  override suspend fun removeProject(id: Long) {
    projectDao.remove(id)
  }
}
