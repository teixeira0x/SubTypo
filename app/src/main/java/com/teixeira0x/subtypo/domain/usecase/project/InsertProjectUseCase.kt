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

package com.teixeira0x.subtypo.domain.usecase.project

import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.domain.repository.project.ProjectRepository
import javax.inject.Inject

class InsertProjectUseCase
@Inject
constructor(private val repository: ProjectRepository) {

  operator suspend fun invoke(project: Project): Long {
    return repository.insertProject(project)
  }
}
