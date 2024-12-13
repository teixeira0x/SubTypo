/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.ui.activity.project.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.teixeira0x.subtypo.databinding.FragmentSubtitleListBinding
import com.teixeira0x.subtypo.ui.activity.project.adapter.SubtitleListAdapter
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.SubtitleViewModel
import com.teixeira0x.subtypo.ui.activity.project.viewmodel.SubtitleViewModel.SubtitleState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubtitleListFragment : Fragment() {

  private var _binding: FragmentSubtitleListBinding? = null

  private val binding: FragmentSubtitleListBinding
    get() = checkNotNull(_binding) { "SubtitleListFragment has been destroyed" }

  private val subtitleViewModel by
    viewModels<SubtitleViewModel>(ownerProducer = { requireActivity() })

  private val subtitleListAdapter by lazy {
    SubtitleListAdapter(
      onSubtitleClick = { subtitle -> },
      editSubtitle = { subtitle -> },
      deleteSubtitle = { subtitle ->
        subtitleViewModel.removeSubtitle(subtitle.id)
      },
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentSubtitleListBinding.inflate(inflater, container, false)
      .also { _binding = it }
      .root
  }

  override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
    binding.rvSubtitles.layoutManager = LinearLayoutManager(requireContext())
    binding.rvSubtitles.adapter = subtitleListAdapter
    observeViewModel()
  }

  private fun observeViewModel() {
    subtitleViewModel.stateData.observe(this) { state ->
      when (state) {
        is SubtitleState.Loading -> {
          binding.rvSubtitles.isVisible = false
          binding.noSubtitles.isVisible = false
          binding.progress.isVisible = true
        }
        is SubtitleState.Loaded -> {
          binding.rvSubtitles.isVisible = true
          binding.noSubtitles.isVisible = state.subtitles.isEmpty()
          binding.progress.isVisible = false
          subtitleListAdapter.submitList(
            state.subtitles,
            state.selectedSubtitle?.id ?: 0,
          )
        }
        is SubtitleState.Removed -> {}
        is SubtitleState.Error -> {}
      }
    }
  }
}
