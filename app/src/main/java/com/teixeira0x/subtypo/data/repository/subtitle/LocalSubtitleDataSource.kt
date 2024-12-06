package com.teixeira0x.subtypo.data.repository.subtitle

import com.teixeira0x.subtypo.data.db.SubTypoDatabase
import com.teixeira0x.subtypo.data.mapper.SubtitleDataMapper.toEntity
import com.teixeira0x.subtypo.data.mapper.SubtitleDataMapper.toModel
import com.teixeira0x.subtypo.domain.model.Subtitle
import com.teixeira0x.subtypo.domain.repository.subtitle.SubtitleDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalSubtitleDataSource
@Inject
constructor(private val db: SubTypoDatabase) : SubtitleDataSource {

  private val subtitleDao = db.subtitleDao

  override fun getAll(): Flow<List<Subtitle>> {
    return subtitleDao.getAll().map { subtitles ->
      subtitles.map { it.toModel() }
    }
  }

  override fun getSubtitle(id: Long): Flow<Subtitle?> {
    return subtitleDao.findById(id).map { it?.toModel() }
  }

  override suspend fun insertSubtitle(subtitle: Subtitle): Long {
    return subtitleDao.insert(subtitle.toEntity())
  }

  override suspend fun updateSubtitle(subtitle: Subtitle) {
    subtitleDao.update(subtitle.toEntity())
  }

  override suspend fun removeSubtitle(id: Long) {
    subtitleDao.remove(id)
  }
}
