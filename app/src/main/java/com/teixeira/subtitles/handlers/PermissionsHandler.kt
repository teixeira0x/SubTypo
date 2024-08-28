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

package com.teixeira.subtitles.handlers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira.subtitles.R

/**
 * Manages the permissions required for the application to function.
 *
 * @author Felipe Teixeira
 */
public class PermissionsHandler(val activity: Activity, val registry: ActivityResultRegistry) :
  DefaultLifecycleObserver {

  companion object {
    const val KEY_PERMISSION = "key_read_media_video_files"

    @JvmStatic
    fun Context.isPermissionsGranted(): Boolean {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) ==
          PackageManager.PERMISSION_GRANTED
      } else {
        checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
          PackageManager.PERMISSION_GRANTED
      }
    }
  }

  private lateinit var reqPermissions: ActivityResultLauncher<Array<String>>

  private val readMediaPermissions =
    arrayOf(
      Manifest.permission
        .READ_EXTERNAL_STORAGE, // [READ_EXTERNAL_STORAGE] For devices below api 33 (Android 13).
      Manifest.permission
        .READ_MEDIA_VIDEO, // [READ_MEDIA_VIDEO] For devices with api 33 (Android 13) or above.
    )

  override fun onCreate(owner: LifecycleOwner) {
    reqPermissions =
      registry.register(
        KEY_PERMISSION,
        owner,
        ActivityResultContracts.RequestMultiplePermissions(),
        this::onResultCallback,
      )

    requestPermissions()
  }

  override fun onDestroy(owner: LifecycleOwner) {
    reqPermissions.unregister()
  }

  /** Launch the permission request on the screen. */
  private fun requestPermissions() {
    reqPermissions.launch(readMediaPermissions)
  }

  /**
   * Called when granting or denying permissions and checking whether permission was granted.
   *
   * @param permissions Result of requested permissions.
   */
  private fun onResultCallback(permissions: Map<String, Boolean>) {
    if (permissions.isNotEmpty()) {
      val permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          permissions.entries.elementAt(1)
        } else {
          permissions.entries.elementAt(0)
        }

      checkPermission(permission)
    }
  }

  /**
   * Checks whether the given permission was granted or denied, if it was denied it shows a dialog
   * explaining why the permission was requested, and if it is denied more than once Android no
   * longer launches the request on the screen so the user has to grant the permissions manually.
   *
   * @param permission Permission to perform verification.
   */
  private fun checkPermission(permission: Map.Entry<String, Boolean>) {
    when {
      activity.shouldShowRequestPermissionRationale(permission.key) -> {
        // Show why the user should enable permissions.
        showRequestPermissionDialog()
      }
      permission.value -> {
        // Permission Granted.
      }
      else -> {
        // If permission is denied too many times, the device will block the request, so the user
        // will have to go to the app's settings and grant it manually.
        showPermissionSettingsDialog()
      }
    }
  }

  /** Displays a dialog requesting permissions. */
  private fun showRequestPermissionDialog() {
    MaterialAlertDialogBuilder(activity).apply {
      setTitle(R.string.msg_permission_request)
      setMessage(R.string.msg_permission_request_detail)
      setPositiveButton(R.string.grant) { _, _ -> requestPermissions() }
      setNegativeButton(R.string.no, null)
      show()
    }
  }

  /** Displays a dialog asking the user to go to the application settings to grant permissions. */
  private fun showPermissionSettingsDialog() {
    MaterialAlertDialogBuilder(activity).apply {
      setTitle(R.string.msg_permission_request)
      setMessage(R.string.msg_permission_request_settings_detail)
      setPositiveButton(R.string.grant) { _, _ ->
        activity.startActivity(
          Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
          }
        )
      }
      setNegativeButton(R.string.no, null)
      show()
    }
  }
}
