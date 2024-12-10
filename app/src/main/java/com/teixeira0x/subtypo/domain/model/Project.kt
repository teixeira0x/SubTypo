package com.teixeira0x.subtypo.domain.model

data class Project(
  val id: Long = 0,
  val name: String,
  val videoUri: String,
  val cues: List<Cue>,
) {
  val videoName: String
    get() = videoUri.substringAfterLast("/")
}
