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

package com.teixeira0x.subtypo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.teixeira0x.subtypo.data.db.converter.CueConverter
import com.teixeira0x.subtypo.data.db.dao.ProjectDao
import com.teixeira0x.subtypo.data.db.entity.ProjectEntity

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = true)
@TypeConverters(CueConverter::class)
abstract class SubTypoDatabase : RoomDatabase() {
  abstract val projectDao: ProjectDao
}
