package com.teixeira0x.subtypo.domain.model

data class ProjectData(
  val id: Long,
  val name: String,
  val videoUri: String,
  val subtitles: List<Subtitle>,
)
