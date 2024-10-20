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

package com.teixeira.subtitles.ext

import android.content.Context
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.teixeira.subtitles.ui.dialog.ProgressDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Calls [CoroutineScope.cancel] only if a job is active in the scope.
 *
 * @param message Optional message describing the cause of the cancellation.
 * @param cause Optional cause of the cancellation.
 * @see cancelIfActive
 * @author Akash Yadav.
 */
fun CoroutineScope.cancelIfActive(message: String, cause: Throwable? = null) =
  cancelIfActive(CancellationException(message, cause))

/**
 * Calls [CoroutineScope.cancel] only if a job is active in the scope.
 *
 * @param exception Optional cause of the cancellation.
 * @author Akash Yadav.
 */
fun CoroutineScope.cancelIfActive(exception: CancellationException? = null) {
  val job = coroutineContext[Job]
  job?.cancel(exception)
}

inline fun CoroutineScope.launchWithProgress(
  uiContext: Context,
  crossinline configureBuilder: ProgressDialogBuilder.() -> Unit = {},
  crossinline invokeOnCompletion: (throwable: Throwable?) -> Unit = {},
  crossinline action: suspend CoroutineScope.() -> Unit,
): Job {
  val builder = ProgressDialogBuilder(uiContext)
  builder.configureBuilder()

  val dialog = builder.show()

  return launch { action() }
    .also { job ->
      job.invokeOnCompletion { throwable ->
        runOnUiThread { dialog.dismiss() }
        invokeOnCompletion(throwable)
      }
    }
}
