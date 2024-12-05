package com.teixeira0x.subtypo

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.blankj.utilcode.util.ThrowableUtils
import com.google.android.material.color.DynamicColors
import com.teixeira0x.subtypo.activities.CrashActivity
import com.teixeira0x.subtypo.preferences.aparenceMaterialYou
import com.teixeira0x.subtypo.preferences.aparenceUIMode

class App : Application() {

  companion object {
    const val APP_REPO_URL = "https://github.com/teixeira0x/SubTypo"
    const val APP_REPO_OPEN_ISSUE = "$APP_REPO_URL/issues/new"

    @JvmStatic
    lateinit var instance: App
      private set
  }

  private var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

  val defaultPrefs by lazy {
    PreferenceManager.getDefaultSharedPreferences(this)
  }

  override fun onCreate() {
    uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException)
    instance = this
    super.onCreate()

    if (aparenceMaterialYou) {
      DynamicColors.applyToActivitiesIfAvailable(this)
    }
    updateUIMode()
  }

  fun updateUIMode() {
    AppCompatDelegate.setDefaultNightMode(aparenceUIMode)
  }

  fun openUrl(url: String) {
    startActivity(
      Intent().apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        action = Intent.ACTION_VIEW
        data = Uri.parse(url)
      }
    )
  }

  private fun uncaughtException(thread: Thread, throwable: Throwable) {
    // Start the crash activity
    startActivity(
      Intent(this, CrashActivity::class.java).apply {
        putExtra(
          CrashActivity.KEY_EXTRA_ERROR,
          ThrowableUtils.getFullStackTrace(throwable),
        )
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    )

    uncaughtExceptionHandler?.uncaughtException(thread, throwable)
    System.exit(1)
  }
}
