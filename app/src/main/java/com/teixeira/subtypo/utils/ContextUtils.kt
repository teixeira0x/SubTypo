package com.teixeira.subtypo.utils

import android.content.Context
import android.view.LayoutInflater

object ContextUtils {

  val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)
}
