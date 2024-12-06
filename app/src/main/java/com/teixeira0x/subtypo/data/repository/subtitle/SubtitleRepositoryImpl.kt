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

import com.teixeira0x.subtypo.domain.model.Subtitle
import com.teixeira0x.subtypo.domain.repository.subtitle.SubtitleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SubtitleRepositoryImpl
@Inject
constructor(private val localDataSource: LocalSubtitleDataSource) :
  SubtitleRepository {

  override fun getAll(): Flow<List<Subtitle>> {
    return localDataSource.getAll()
  }

  override fun getSubtitle(id: Long): Flow<Subtitle?> {
    return localDataSource.getSubtitle(id)
  }

  override suspend fun insertSubtitle(subtitle: Subtitle): Long {
    return localDataSource.insertSubtitle(subtitle)
  }

  override suspend fun updateSubtitle(subtitle: Subtitle) {
    localDataSource.updateSubtitle(subtitle)
  }

  override suspend fun removeSubtitle(id: Long) {
    localDataSource.removeSubtitle(id)
  }
}
