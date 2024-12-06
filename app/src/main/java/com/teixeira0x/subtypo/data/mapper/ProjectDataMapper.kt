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

package com.teixeira0x.subtypo.data.mapper

import com.teixeira0x.subtypo.data.db.entity.ProjectDataEntity
import com.teixeira0x.subtypo.data.db.entity.ProjectEntity
import com.teixeira0x.subtypo.data.mapper.SubtitleDataMapper.toEntity
import com.teixeira0x.subtypo.data.mapper.SubtitleDataMapper.toModel
import com.teixeira0x.subtypo.domain.model.Project
import com.teixeira0x.subtypo.domain.model.ProjectData

object ProjectDataMapper {

  fun ProjectEntity.toModel(): Project {
    return Project(id = id, name = name, videoUri = videoUri)
  }

  fun ProjectDataEntity.toModel(): ProjectData {
    return ProjectData(
      id = project.id,
      name = project.name,
      videoUri = project.videoUri,
      subtitles = subtitles.map { it.toModel() },
    )
  }

  fun Project.toEntity(): ProjectEntity {
    return ProjectEntity(id = id, name = name, videoUri = videoUri)
  }

  fun ProjectData.toEntity(): ProjectDataEntity {
    return ProjectDataEntity(
      project = ProjectEntity(id = id, name = name, videoUri = videoUri),
      subtitles = subtitles.map { it.toEntity() },
    )
  }
}
