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

package com.teixeira0x.subtypo.ui.activity.crash

import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.DeviceUtils
import com.google.android.material.R.attr
import com.google.android.material.color.MaterialColors
import com.teixeira0x.subtypo.App
import com.teixeira0x.subtypo.BuildConfig
import com.teixeira0x.subtypo.databinding.ActivityCrashBinding
import com.teixeira0x.subtypo.ui.activity.BaseActivity
import java.util.Date

class CrashActivity : BaseActivity() {

  companion object {
    const val KEY_EXTRA_CRASH_ERROR = "key_extra_error"
  }

  private var _binding: ActivityCrashBinding? = null
  private val binding: ActivityCrashBinding
    get() = _binding!!

  private val softwareInfo: String
    get() =
      StringBuilder("Manufacturer: ")
        .append(DeviceUtils.getManufacturer())
        .append("\n")
        .append("Device: ")
        .append(DeviceUtils.getModel())
        .append("\n")
        .append("SDK: ")
        .append(Build.VERSION.SDK_INT)
        .append("\n")
        .append("Android: ")
        .append(Build.VERSION.RELEASE)
        .append("\n")
        .append("Model: ")
        .append(Build.VERSION.INCREMENTAL)
        .append("\n")
        .toString()

  private val appInfo: String
    get() =
      StringBuilder("Version: ")
        .append(BuildConfig.VERSION_NAME)
        .append(". ")
        .append("Build type: ")
        .append(BuildConfig.BUILD_TYPE)
        .toString()

  private val date: Date
    get() = Calendar.getInstance().time

  override val statusBarColor: Int
    get() = MaterialColors.getColor(this, attr.colorOnSurfaceInverse, 0)

  override val navigationBarColor: Int
    get() = MaterialColors.getColor(this, attr.colorOnSurfaceInverse, 0)

  override val navigationBarDividerColor: Int
    get() = MaterialColors.getColor(this, attr.colorOnSurfaceInverse, 0)

  override fun bindView(): View {
    return ActivityCrashBinding.inflate(layoutInflater)
      .also { _binding = it }
      .root
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)

    onBackPressedDispatcher.addCallback(
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          finishAffinity()
        }
      }
    )

    binding.apply {
      tvError.setText(
        StringBuilder()
          .append("$appInfo\n")
          .append("$softwareInfo\n")
          .append("$date\n\n")
          .append(intent.getStringExtra(KEY_EXTRA_CRASH_ERROR))
          .toString()
      )

      btnCopyAndReport.setOnClickListener {
        ClipboardUtils.copyText(tvError.text.toString())
        app.openUrl(App.APP_REPO_OPEN_ISSUE)
      }
      btnCopy.setOnClickListener {
        ClipboardUtils.copyText(tvError.text.toString())
      }
      btnCloseApp.setOnClickListener { finishAffinity() }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}
