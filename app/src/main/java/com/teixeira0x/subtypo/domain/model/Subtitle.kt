package com.teixeira0x.subtypo.domain.model

data class Subtitle(
  val id: Long,
  val projectId: Long,
  val name: String,
  val cues: List<Cue>,
)
