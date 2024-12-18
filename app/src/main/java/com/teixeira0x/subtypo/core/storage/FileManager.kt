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
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class FileManager(private val activity: Activity) {

  private lateinit var pickFileLauncher: ActivityResultLauncher<String>
  private lateinit var saveFileLauncher: ActivityResultLauncher<String>

  var listener: Listener? = null

  init {
    registerLaunchers()
  }

  private fun registerLaunchers() {
    pickFileLauncher =
      (activity as androidx.activity.ComponentActivity)
        .registerForActivityResult(ActivityResultContracts.GetContent()) { uri
          ->
          listener?.onFilePicked(uri)
        }

    saveFileLauncher =
      activity.registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/*")
      ) { uri ->
        listener?.onFileSaved(uri)
      }
  }

  fun launchSaver(fileName: String) = saveFileLauncher.launch(fileName)

  fun launchPicker() = pickFileLauncher.launch("*/*")

  interface Listener {
    fun onFilePicked(uri: Uri?)

    fun onFileSaved(uri: Uri?)
  }
}
