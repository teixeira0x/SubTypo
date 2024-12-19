package com.teixeira0x.subtypo.ui.utils

import android.content.Context
import android.widget.Toast

fun Context.showToastLong(message: Int) {
  showToast(getString(message), Toast.LENGTH_LONG)
}

fun Context.showToastLong(message: String) {
  showToast(message, Toast.LENGTH_LONG)
}

fun Context.showToastShort(message: Int) {
  showToast(getString(message), Toast.LENGTH_SHORT)
}

fun Context.showToastShort(message: String) {
  showToast(message, Toast.LENGTH_SHORT)
}

fun Context.showToast(message: String, duration: Int) =
  Toast.makeText(this, message, duration).show()
