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

package com.teixeira0x.subtypo.ui.activity.about

import android.os.Build
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.google.android.material.R.attr
import com.google.android.material.color.MaterialColors.getColor
import com.mikepenz.aboutlibraries.ui.LibsActivity
import com.teixeira0x.subtypo.R

/**
 * Extends the default aboutlibraries libs activity to add support for the
 * default SubTypo theme style.
 */
class ThemedLibsActivity : LibsActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    window?.apply {
      this.statusBarColor =
        getColor(
          this@ThemedLibsActivity,
          com.google.android.material.R.attr.colorOnSurfaceInverse,
          0,
        )
      this.navigationBarColor =
        getColor(
          this@ThemedLibsActivity,
          com.google.android.material.R.attr.colorSurface,
          0,
        )
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this.navigationBarDividerColor =
          getColor(
            this@ThemedLibsActivity,
            com.google.android.material.R.attr.colorOutlineVariant,
            0,
          )
      }
    }
    super.onCreate(savedInstanceState)

    val toolbar =
      findViewById<Toolbar>(com.mikepenz.aboutlibraries.R.id.toolbar)
    toolbar.setBackgroundResource(R.drawable.shape_toolbar)
  }
}
