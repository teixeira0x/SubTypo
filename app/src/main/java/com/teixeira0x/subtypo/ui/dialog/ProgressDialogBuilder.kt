/*
 * This file is part of Visual Code Space.
 *
 * Visual Code Space is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Visual Code Space is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Visual Code Space.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.databinding.LayoutProgressDialogBinding

class ProgressDialogBuilder(val context: Context) {

  private val binding =
    LayoutProgressDialogBinding.inflate(LayoutInflater.from(context))

  val builder = MaterialAlertDialogBuilder(context).setView(binding.root)

  fun setPositiveButton(
    text: String,
    listener: DialogInterface.OnClickListener,
  ) = apply { builder.setPositiveButton(text, listener) }

  fun setPositiveButton(
    @StringRes text: Int,
    listener: DialogInterface.OnClickListener,
  ) = apply { builder.setPositiveButton(text, listener) }

  fun setNegativeButton(
    text: String,
    listener: DialogInterface.OnClickListener,
  ) = apply { builder.setNegativeButton(text, listener) }

  fun setNegativeButton(
    @StringRes text: Int,
    listener: DialogInterface.OnClickListener,
  ) = apply { builder.setNegativeButton(text, listener) }

  fun setNeutralButton(
    text: String,
    listener: DialogInterface.OnClickListener,
  ) = apply { builder.setNeutralButton(text, listener) }

  fun setNeutralButton(
    @StringRes text: Int,
    listener: DialogInterface.OnClickListener,
  ) = apply { builder.setNeutralButton(text, listener) }

  fun show(): AlertDialog = builder.show()

  fun create(): AlertDialog = builder.create()

  fun setTitle(title: String) = apply { builder.setTitle(title) }

  fun setTitle(@StringRes title: Int) = apply { builder.setTitle(title) }

  fun setMessage(message: String) = apply { binding.message.text = message }

  fun setMessage(@StringRes message: Int) = apply {
    binding.message.setText(message)
  }

  fun setProgress(progress: Int) = apply {
    binding.indicator.setProgressCompat(progress, true)
  }

  fun setMax(max: Int) = apply { binding.indicator.setMax(max) }

  fun setMin(min: Int) = apply { binding.indicator.setMin(min) }

  fun setCancelable(cancelable: Boolean) = apply {
    builder.setCancelable(cancelable)
  }

  fun setIndeterminate(indeterminate: Boolean = true) = apply {
    binding.indicator.isIndeterminate = indeterminate
  }
}
