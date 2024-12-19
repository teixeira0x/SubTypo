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

package com.teixeira0x.subtypo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.teixeira0x.subtypo.data.db.entity.SubtitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtitleDao {

  @Query("SELECT * FROM subtitles WHERE projectId = :projectId")
  fun getAll(projectId: Long): Flow<List<SubtitleEntity>>

  @Query("SELECT * FROM subtitles WHERE id = :id")
  fun findById(id: Long): Flow<SubtitleEntity?>

  @Insert suspend fun insert(subtitle: SubtitleEntity): Long

  @Update(entity = SubtitleEntity::class)
  suspend fun update(subtitle: SubtitleEntity)

  @Query("DELETE FROM subtitles WHERE id = :id")
  suspend fun remove(id: Long): Int
}
