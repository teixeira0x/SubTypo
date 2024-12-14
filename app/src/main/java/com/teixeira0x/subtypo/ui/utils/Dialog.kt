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

package com.teixeira0x.subtypo.ui.utils

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R

fun Context.showConfirmDialog(
  title: Int,
  message: Int,
  cancelable: Boolean = true,
  onNegativeClick: (DialogInterface, Int) -> Unit = { _, _ -> },
  onPositiveClick: (DialogInterface, Int) -> Unit = { _, _ -> },
) {
  showConfirmDialog(
    title = getString(title),
    message = getString(message),
    cancelable = cancelable,
    onNegativeClick = onNegativeClick,
    onPositiveClick = onPositiveClick,
  )
}

fun Context.showConfirmDialog(
  title: String,
  message: String,
  cancelable: Boolean = true,
  onNegativeClick: (DialogInterface, Int) -> Unit = { _, _ -> },
  onPositiveClick: (DialogInterface, Int) -> Unit = { _, _ -> },
) {
  MaterialAlertDialogBuilder(this)
    .setTitle(title)
    .setMessage(message)
    .setCancelable(cancelable)
    .setNegativeButton(R.string.no, onNegativeClick)
    .setPositiveButton(R.string.yes, onPositiveClick)
    .show()
}

fun Context.showDialog(
  title: String,
  message: String,
  cancelable: Boolean = true,
  negativeText: String? = null,
  positiveText: String? = null,
  onNegativeClick: ((DialogInterface, Int) -> Unit)? = null,
  onPositiveClick: ((DialogInterface, Int) -> Unit)? = null,
) {
  MaterialAlertDialogBuilder(this).apply {
    setTitle(title)
    setMessage(message)
    setCancelable(cancelable)
    if (negativeText != null) {
      setNegativeButton(negativeText, onNegativeClick)
    }
    if (positiveText != null) {
      setPositiveButton(positiveText, onPositiveClick)
    }
    show()
  }
}
