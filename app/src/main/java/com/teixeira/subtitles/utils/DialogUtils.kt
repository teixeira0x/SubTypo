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

package com.teixeira.subtitles.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira.subtitles.databinding.LayoutProgressDialogBinding
import com.teixeira.subtitles.utils.ContextUtils.layoutInflater

/** @author Felipe Teixeira */
object DialogUtils {

  /**
   * Creates an instance of a custom dialog with a round progress bar and an immutable message.
   *
   * @param message Message to be displayed during progress.
   * @param cancelable Sets whether the dialog is cancelable [false] by default.
   * @return The dialog builder with the custom view.
   */
  fun Context.newProgressDialog(
    message: String,
    cancelable: Boolean = false,
  ): MaterialAlertDialogBuilder {
    return MaterialAlertDialogBuilder(this)
      .setView(
        LayoutProgressDialogBinding.inflate(this.layoutInflater)
          .apply { this.message.text = message }
          .root
      )
      .setCancelable(cancelable)
  }
}
