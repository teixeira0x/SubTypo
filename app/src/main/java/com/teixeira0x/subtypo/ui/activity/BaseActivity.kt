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

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import com.teixeira0x.subtypo.App
import com.teixeira0x.subtypo.ui.fragment.dialog.ProgressDialogFragment

/**
 * Base activity for all application activities.
 *
 * @author Felipe Teixeira
 */
abstract class BaseActivity : AppCompatActivity() {

  private var progressDialog: ProgressDialogFragment? = null

  protected open val statusBarColor: Int
    get() = MaterialColors.getColor(this, R.attr.colorSurface, 0)

  protected open val navigationBarColor: Int
    get() = MaterialColors.getColor(this, R.attr.colorSurface, 0)

  protected open val navigationBarDividerColor: Int
    get() = MaterialColors.getColor(this, R.attr.colorOutlineVariant, 0)

  protected val app: App
    get() = App.instance

  protected abstract fun bindView(): View

  override fun onCreate(savedInstanceState: Bundle?) {
    window?.apply {
      this.statusBarColor = this@BaseActivity.statusBarColor
      this.navigationBarColor = this@BaseActivity.navigationBarColor
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this.navigationBarDividerColor =
          this@BaseActivity.navigationBarDividerColor
      }
    }
    super.onCreate(savedInstanceState)
    setContentView(bindView())
  }

  protected fun showProgressDialog(message: String? = null) {
    ProgressDialogFragment.newInstance(message)
      .also {
        dismissProgressDialog() // Dismiss previous dialog
        progressDialog = it
      }
      .show(supportFragmentManager, null)
  }

  protected fun dismissProgressDialog() {
    progressDialog?.also { it.dismiss() }
    progressDialog = null
  }
}
