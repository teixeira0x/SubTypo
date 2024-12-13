package com.teixeira0x.subtypo.domain.model

data class Subtitle(
  val id: Long = 0,
  val projectId: Long,
  val name: String,
  val cues: List<Cue>,
)
