package com.teixeira0x.subtypo.ui.utils.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.checkSelfPermission

fun Context.checkPermissions(permissions: Array<String>): Boolean {
  return permissions.all { perm ->
    checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
  }
}
