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

package com.teixeira0x.subtypo.ui.projecteditor.mvi

import android.net.Uri

sealed class ProjectEditorIntent {
  data class Load(val id: Long) : ProjectEditorIntent()

  data class UpdateVideoUri(val uri: Uri?) : ProjectEditorIntent()

  data class Create(val name: String, val videoUri: String) :
    ProjectEditorIntent()

  data class Update(val id: Long, val name: String, val videoUri: String) :
    ProjectEditorIntent()
}
