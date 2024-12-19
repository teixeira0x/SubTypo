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

package com.teixeira0x.subtypo.data.repository.subtitle

import com.teixeira0x.subtypo.data.db.SubTypoDatabase
import com.teixeira0x.subtypo.data.mapper.SubtitleDataMapper.toEntity
import com.teixeira0x.subtypo.data.mapper.SubtitleDataMapper.toModel
import com.teixeira0x.subtypo.domain.model.Subtitle
import com.teixeira0x.subtypo.domain.repository.subtitle.SubtitleDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalSubtitleDataSource
@Inject
constructor(private val db: SubTypoDatabase) : SubtitleDataSource {

  private val subtitleDao = db.subtitleDao

  override fun getAll(projectId: Long): Flow<List<Subtitle>> {
    return subtitleDao.getAll(projectId).map { subtitles ->
      subtitles.map { it.toModel() }
    }
  }

  override fun getSubtitle(id: Long): Flow<Subtitle?> {
    return subtitleDao.findById(id).map { it?.toModel() }
  }

  override suspend fun insertSubtitle(subtitle: Subtitle): Long {
    return subtitleDao.insert(subtitle.toEntity())
  }

  override suspend fun updateSubtitle(subtitle: Subtitle) {
    subtitleDao.update(subtitle.toEntity())
  }

  override suspend fun removeSubtitle(id: Long): Int {
    return subtitleDao.remove(id)
  }
}
