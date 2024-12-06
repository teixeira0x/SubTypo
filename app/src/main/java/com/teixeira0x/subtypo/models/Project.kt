package com.teixeira0x.subtypo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Project(
  var id: String,
  var path: String,
  var name: String,
  var videoPath: String,
  val version: String,
) : Parcelable, Comparable<Project> {

  val videoName: String
    get() = videoPath.substringAfterLast("/")

  override fun compareTo(other: Project): Int {
    return if (id.toInt() > other.id.toInt()) -1 else 0
  }
}
