package com.teixeira0x.subtypo.ui.activity

import android.content.Context
import android.content.Intent
import com.teixeira0x.subtypo.ui.activity.project.ProjectActivity
import com.teixeira0x.subtypo.utils.Constants

/** Navigator to facilitate navigation between activities. */
object Navigator {

  /**
   * Navigates to the ProjectActivity.
   *
   * @param projectId The ID of the project to open.
   */
  fun navigateToProjectActivity(context: Context, projectId: Long) {
    val intent =
      Intent(context, ProjectActivity::class.java).apply {
        putExtra(Constants.KEY_PROJECT_ID_ARG, projectId)
      }
    context.startActivity(intent)
  }
}
