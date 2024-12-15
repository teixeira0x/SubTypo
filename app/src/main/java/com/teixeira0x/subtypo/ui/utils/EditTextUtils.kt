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

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Useful functions to ease the implementation of {@link TextWatcher}.
 *
 * @author Felipe Teixeira
 */
object EditTextUtils {

  inline fun EditText.beforeTextChanged(
    crossinline beforeTextChangedCallback:
      (s: CharSequence, start: Int, count: Int, after: Int) -> Unit
  ) {
    addTextChangedListener(
      object : DefaultTextWatcher() {
        override fun beforeTextChanged(
          s: CharSequence,
          start: Int,
          count: Int,
          after: Int,
        ) {
          beforeTextChangedCallback(s, start, count, after)
        }
      }
    )
  }

  inline fun EditText.onTextChanged(
    crossinline onTextChangedCallback:
      (s: CharSequence, start: Int, count: Int, after: Int) -> Unit
  ) {
    addTextChangedListener(
      object : DefaultTextWatcher() {
        override fun onTextChanged(
          s: CharSequence,
          start: Int,
          count: Int,
          after: Int,
        ) {
          onTextChangedCallback(s, start, count, after)
        }
      }
    )
  }

  inline fun EditText.afterTextChanged(
    crossinline afterTextChangedCallback: (s: Editable) -> Unit
  ) {
    addTextChangedListener(
      object : DefaultTextWatcher() {
        override fun afterTextChanged(s: Editable) {
          afterTextChangedCallback(s)
        }
      }
    )
  }
}

/**
 * A base implementation for {@link TextWatcher} avoids implementing methods
 * that you won't use from {@link TextWatcher}
 */
open class DefaultTextWatcher : TextWatcher {

  override fun beforeTextChanged(
    s: CharSequence,
    start: Int,
    count: Int,
    after: Int,
  ) {
    // Do nothing
  }

  override fun onTextChanged(
    s: CharSequence,
    start: Int,
    count: Int,
    after: Int,
  ) {
    // Do nothing
  }

  override fun afterTextChanged(s: Editable) {
    // Do nothing
  }
}
