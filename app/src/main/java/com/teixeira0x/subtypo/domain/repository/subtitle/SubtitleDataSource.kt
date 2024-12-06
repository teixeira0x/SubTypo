package com.teixeira0x.subtypo.domain.repository.subtitle

import com.teixeira0x.subtypo.domain.model.Subtitle
import kotlinx.coroutines.flow.Flow

interface SubtitleDataSource {
  fun getAll(): Flow<List<Subtitle>>

  fun getSubtitle(id: Long): Flow<Subtitle?>

  suspend fun insertSubtitle(subtitle: Subtitle): Long

  suspend fun updateSubtitle(subtitle: Subtitle)

  suspend fun removeSubtitle(id: Long)
}
