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

package com.teixeira0x.subtypo.ui.activity.main.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.ui.utils.permission.checkPermissions

/**
 * Manages the permissions required for the application to function.
 *
 * @author Felipe Teixeira
 */
public class PermissionsHandler(
  val activity: Activity,
  val registry: ActivityResultRegistry,
) : DefaultLifecycleObserver {

  companion object {
    private const val KEY_PERMISSION = "key_read_media_video_files"

    private val permissions: Array<String>
      get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
          arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
          )
        }

    @JvmStatic
    fun Context.isPermissionsGranted(): Boolean {
      return checkPermissions(permissions)
    }

    /**
     * Displays a dialog asking the user to go to the application settings to
     * grant permissions.
     */
    @JvmStatic
    fun showPermissionSettingsDialog(context: Context) {
      MaterialAlertDialogBuilder(context)
        .setTitle(R.string.permission_required)
        .setMessage(R.string.permission_required_settings_detail)
        .setPositiveButton(R.string.permission_required_settings_goto) { _, _ ->
          context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
              data = Uri.fromParts("package", context.packageName, null)
            }
          )
        }
        .setNegativeButton(R.string.no, null)
        .show()
    }
  }

  private lateinit var reqPermissions: ActivityResultLauncher<Array<String>>

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

  /**
   * Called when granting or denying permissions and checking whether permission
   * was granted.
   *
   * @param permissions Result of requested permissions.
   */
  private fun onResultCallback(permissions: Map<String, Boolean>) {
    for (entry in permissions.entries) {
      checkPermission(entry)
    }
  }

  /**
   * Checks whether the given permission was granted or denied, if it was denied
   * it shows a dialog explaining why the permission was requested, and if it is
   * denied more than once Android no longer launches the request on the screen
   * so the user has to grant the permissions manually.
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
        // If permission is denied too many times, the device will block the
        // request, so the user
        // will have to go to the app's settings and grant it manually.
        showPermissionSettingsDialog(activity)
      }
    }
  }

  /** Displays a dialog requesting permissions. */
  private fun showRequestPermissionDialog() {
    MaterialAlertDialogBuilder(activity)
      .setTitle(R.string.permission_required)
      .setMessage(R.string.permission_required_detail)
      .setPositiveButton(R.string.grant) { _, _ -> requestPermissions() }
      .setNegativeButton(R.string.no, null)
      .show()
  }

  /** Launch the permission request on the screen. */
  fun requestPermissions() {
    reqPermissions.launch(permissions)
  }
}
