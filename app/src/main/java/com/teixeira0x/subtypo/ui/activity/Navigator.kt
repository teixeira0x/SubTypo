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

package com.teixeira0x.subtypo.ui.activity

import android.content.Context
import android.content.Intent
import com.teixeira0x.subtypo.ui.activity.project.ProjectActivity
import com.teixeira0x.subtypo.utils.Constants

/** Navigator to facilitate navigation between activities. */
object Navigator {

  /**
   * Navigates to the ProjectActivity.
   *
   * @param projectId The ID of the project to open.
   */
  fun navigateToProjectActivity(context: Context, projectId: Long) {
    val intent =
      Intent(context, ProjectActivity::class.java).apply {
        putExtra(Constants.KEY_PROJECT_ID_ARG, projectId)
      }
    context.startActivity(intent)
  }
}
