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

package com.teixeira0x.subtypo.core.storage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class FileManager(private val activity: Activity) {

  private lateinit var pickFileLauncher: ActivityResultLauncher<Intent>
  private lateinit var saveFileLauncher: ActivityResultLauncher<String>
  var onFilePicked: ((Uri?) -> Unit)? = null
  var onFileSaved: ((Uri?) -> Unit)? = null

  init {
    registerLaunchers()
  }

  private fun registerLaunchers() {
    pickFileLauncher =
      (activity as androidx.activity.ComponentActivity)
        .registerForActivityResult(
          ActivityResultContracts.StartActivityForResult()
        ) { result ->
          if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            onFilePicked?.invoke(uri)
          } else {
            onFilePicked?.invoke(null)
          }
        }

    saveFileLauncher =
      activity.registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/*")
      ) { uri ->
        onFileSaved?.invoke(uri)
      }
  }

  fun launchSaver(fileName: String) {
    saveFileLauncher.launch(fileName)
  }

  fun launchPicker() {
    val intent = Intent(Intent.ACTION_PICK).apply { type = "*/*" }
    pickFileLauncher.launch(intent)
  }
}
