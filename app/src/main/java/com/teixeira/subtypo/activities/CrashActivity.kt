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

package com.teixeira.subtypo.activities

import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.DeviceUtils
import com.teixeira.subtypo.App
import com.teixeira.subtypo.BuildConfig
import com.teixeira.subtypo.databinding.ActivityCrashBinding
import java.util.Date

/**
 * Activity to handle application crash.
 *
 * @author Felipe Teixeira
 */
class CrashActivity : BaseActivity() {

  companion object {
    const val KEY_EXTRA_ERROR = "key_extra_error"
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
        .append("\n")
        .append("Build: ")
        .append(BuildConfig.BUILD_TYPE)
        .toString()

  private val date: Date
    get() = Calendar.getInstance().time

  override fun bindView(): View {
    _binding = ActivityCrashBinding.inflate(layoutInflater)
    return binding.root
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

    binding.log.text =
      StringBuilder()
        .append("$softwareInfo\n")
        .append("$appInfo\n\n")
        .append("$date\n\n")
        .append(intent.getStringExtra(KEY_EXTRA_ERROR))
        .toString()

    binding.copyAndReportIssue.setOnClickListener {
      ClipboardUtils.copyText(binding.log.text.toString())
      app.openUrl(App.APP_REPO_OPEN_ISSUE)
    }
    binding.copy.setOnClickListener { ClipboardUtils.copyText(binding.log.text.toString()) }
    binding.closeApp.setOnClickListener { finishAffinity() }
  }
}
